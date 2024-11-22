package com.apaz.lambda.url.redirect.exception;

public class JsonToBodyException extends RuntimeException {
    public JsonToBodyException(String error, Exception e) {
        super(error, e);
    }
}
