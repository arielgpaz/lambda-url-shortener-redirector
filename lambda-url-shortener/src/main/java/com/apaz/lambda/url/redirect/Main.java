package com.apaz.lambda.url.redirect;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.apaz.lambda.url.redirect.exception.BodyToJsonException;
import com.apaz.lambda.url.redirect.exception.SaveUrlDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    private static final String BUCKET = "lambda-url-shortener-storage";
    private static final String FILE_EXTENSION = ".json";

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log(format("Starting URL shortening process: %s", input));

        var urlData = this.getUrlData(input, context);

        var code = UUID.randomUUID().toString().substring(0, 8);

        this.putUrlDataOnS3(code, urlData, context);

        context.getLogger().log("Finished URL shortening process.");
        return this.buildResponse(code, urlData);
    }

    private UrlData getUrlData(Map<String, Object> input, Context context) {

        var requestBody = this.getRequestBodyMap(input, context);

        String originalUrl = requestBody.get("originalUrl");
        String ttl = requestBody.get("ttl");

        return new UrlData(originalUrl, ttl);
    }

    private Map<String, String> getRequestBodyMap(Map<String, Object> input, Context context) {
        try {
            String body = input.get("body").toString();
            return objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            context.getLogger().log(format("Error parsing request data: %s", e.getMessage()));
            throw new BodyToJsonException("Error parsing JSON request body to Map: " + e.getMessage(), e);
        }
    }

    private void putUrlDataOnS3(String code, UrlData urlData, Context context) {
        try {
            context.getLogger().log(format("Saving shortened URL info to S3: %s", urlData.toString()));
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(code + FILE_EXTENSION)
                    .build();

            String urlDataJson = objectMapper.writeValueAsString(urlData);
            s3Client.putObject(putObjectRequest, RequestBody.fromString(urlDataJson));
        } catch (Exception e) {
            context.getLogger().log(format("Error saving shortened URL info to S3: %s", e.getMessage()));
            throw new SaveUrlDataException("Error saving URL data to S3: " + e.getMessage(), e);
        }
    }

    private Map<String, String> buildResponse(String code, UrlData urlData) {
        Map<String, String> result = new HashMap<>();
        result.put("code", code);
        result.put("originalUrl", urlData.getOriginalUrl());
        result.put("expirationInstant", String.valueOf(urlData.getExpirationInstant()));
        return result;
    }
}