package com.apaz.lambda.url.redirect.exception;

public class BodyToJsonException extends RuntimeException {
    public BodyToJsonException(String error, Exception e) {
        super(error, e);
    }
}
