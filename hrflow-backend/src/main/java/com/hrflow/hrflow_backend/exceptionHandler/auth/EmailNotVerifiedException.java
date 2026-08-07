package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class EmailNotVerifiedException extends BaseException {
    public EmailNotVerifiedException(String message) {
        super(message, "EMAIL_NOT_VERIFIED");
    }
}

