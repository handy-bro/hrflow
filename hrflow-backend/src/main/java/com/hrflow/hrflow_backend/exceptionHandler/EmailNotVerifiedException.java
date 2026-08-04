package com.hrflow.hrflow_backend.exceptionHandler;
public class EmailNotVerifiedException extends BaseException {
    public EmailNotVerifiedException(String message) {
        super(message, "EMAIL_NOT_VERIFIED");
    }
}

