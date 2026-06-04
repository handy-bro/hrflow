package com.hrflow.hrflow_backend.exceptionHandler;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
