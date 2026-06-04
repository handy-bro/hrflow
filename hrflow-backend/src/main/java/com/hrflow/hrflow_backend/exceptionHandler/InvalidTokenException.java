package com.hrflow.hrflow_backend.exceptionHandler;
public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
