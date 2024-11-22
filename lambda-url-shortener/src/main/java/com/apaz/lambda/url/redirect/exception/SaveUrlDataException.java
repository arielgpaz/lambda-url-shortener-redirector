package com.apaz.lambda.url.redirect.exception;

public class SaveUrlDataException extends RuntimeException {
    public SaveUrlDataException(String error, Exception e) {
        super(error, e);
    }
}
