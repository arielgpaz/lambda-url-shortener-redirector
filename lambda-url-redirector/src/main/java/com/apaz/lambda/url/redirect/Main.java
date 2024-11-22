package com.apaz.lambda.url.redirect;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.apaz.lambda.url.redirect.exception.GetUrlDataException;
import com.apaz.lambda.url.redirect.exception.JsonToBodyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    private static final String BUCKET = "lambda-url-shortener-storage";
    private static final String FILE_EXTENSION = ".json";

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log(format("Starting URL redirecting process: %s", input));

        var urlData = this.getUrlData(input, context);

        if (isExpired(urlData)) {
            context.getLogger().log(format("Expired URL data: %s", urlData));
            return this.buildExpiredResponse();
        }

        context.getLogger().log("Finishing URL redirecting process.");
        return this.buildResponse(urlData);
    }

    private UrlData getUrlData(Map<String, Object> input, Context context) {
        try {
            var s3ObjectStream = this.getS3File(input, context);
            return objectMapper.readValue(s3ObjectStream, UrlData.class);
        } catch (IOException e) {
            context.getLogger().log(format("Error parsing url data: %s", e.getMessage()));
            throw new JsonToBodyException("Error parsing JSON response body to object: " + e.getMessage(), e);
        }
    }

    private InputStream getS3File(Map<String, Object> input, Context context) {
        try {
            var code = this.getCode(input);

            context.getLogger().log(format("Getting URL info from S3: %s", code));

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(code + FILE_EXTENSION)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            context.getLogger().log(format("Error getting URL info from S3: %s", e.getMessage()));
            throw new GetUrlDataException("Error getting URL data from S3: " + e.getMessage(), e);
        }
    }

    private String getCode(Map<String, Object> input) {
        var pathParameters = input.get("rawPath").toString();
        return pathParameters.replace("/", "");
    }

    private static boolean isExpired(UrlData urlData) {
        return System.currentTimeMillis() > urlData.getExpirationInstant();
    }

    private Map<String, Object> buildExpiredResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 410);
        response.put("body", "This URL has expired");
        return response;
    }

    private Map<String, Object> buildResponse(UrlData urlData) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 302);
        response.put("headers", headers);
        return response;
    }
}