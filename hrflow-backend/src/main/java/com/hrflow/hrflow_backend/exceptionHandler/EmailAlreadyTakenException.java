package com.hrflow.hrflow_backend.exceptionHandler;
public class EmailAlreadyTakenException extends BaseException {
    public EmailAlreadyTakenException(String message) {
        super(message, "EMAIL_ALREADY_TAKEN");
    }
}
