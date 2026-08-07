package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class PasswordMismatchException extends BaseException {
    public PasswordMismatchException(String message) {
        super(message, "PASSWORD_MISMATCH");
    }
}
