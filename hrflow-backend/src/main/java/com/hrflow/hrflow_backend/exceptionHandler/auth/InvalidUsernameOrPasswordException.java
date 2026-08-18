package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InvalidUsernameOrPasswordException extends BaseException {
    public InvalidUsernameOrPasswordException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
}
