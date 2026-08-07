package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class EmailAlreadyTakenException extends BaseException {
    public EmailAlreadyTakenException(String message) {
        super(message, "EMAIL_ALREADY_TAKEN");
    }
}
