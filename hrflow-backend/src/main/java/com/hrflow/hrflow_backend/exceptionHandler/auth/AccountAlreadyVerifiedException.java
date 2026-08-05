package com.hrflow.hrflow_backend.exceptionHandler.auth;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class AccountAlreadyVerifiedException extends BaseException {
    public AccountAlreadyVerifiedException(String message) {
        super(message, "ACCOUNT_ALREADY_VERIFIED");
    }
}
