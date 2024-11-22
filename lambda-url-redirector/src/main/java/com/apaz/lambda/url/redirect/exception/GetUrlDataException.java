package com.apaz.lambda.url.redirect.exception;

public class GetUrlDataException extends RuntimeException {
    public GetUrlDataException(String error, Exception e) {
        super(error, e);
    }
}
