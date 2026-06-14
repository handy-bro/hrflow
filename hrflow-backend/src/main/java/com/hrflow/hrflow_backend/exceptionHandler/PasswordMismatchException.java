package com.hrflow.hrflow_backend.exceptionHandler;

public class PasswordMismatchException extends BaseException {
    public PasswordMismatchException(String message) {
        super(message, "PASSWORD_MISMATCH");
    }
}
