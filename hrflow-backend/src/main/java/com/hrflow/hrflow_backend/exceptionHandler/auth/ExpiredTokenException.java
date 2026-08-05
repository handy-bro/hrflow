package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class ExpiredTokenException extends BaseException {
    public ExpiredTokenException(String message) {
        super(message, "EXPIRED_TOKEN");
    }
}
