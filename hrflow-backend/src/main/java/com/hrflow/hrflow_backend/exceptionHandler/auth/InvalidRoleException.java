package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InvalidRoleException extends BaseException {
    public InvalidRoleException(String message) {
        super(message, "INVALID_ROLE");
    }
}
