package com.hrflow.hrflow_backend.exceptionHandler;
public class InvalidRoleException extends BaseException {
    public InvalidRoleException(String message) {
        super(message, "INVALID_ROLE");
    }
}
