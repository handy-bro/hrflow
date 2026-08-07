package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
