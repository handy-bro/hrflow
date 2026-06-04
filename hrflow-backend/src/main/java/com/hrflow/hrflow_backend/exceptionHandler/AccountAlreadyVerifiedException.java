package com.hrflow.hrflow_backend.exceptionHandler;

public class AccountAlreadyVerifiedException extends BaseException{
    public AccountAlreadyVerifiedException(String message) {
        super(message, "ACCOUNT_ALREADY_VERIFIED");
    }
}
