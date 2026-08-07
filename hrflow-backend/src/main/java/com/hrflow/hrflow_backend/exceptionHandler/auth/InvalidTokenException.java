package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
