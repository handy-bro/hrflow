package com.hrflow.hrflow_backend.exceptionHandler;
public class ExpiredTokenException extends BaseException {
    public ExpiredTokenException(String message) {
        super(message, "EXPIRED_TOKEN");
    }
}
