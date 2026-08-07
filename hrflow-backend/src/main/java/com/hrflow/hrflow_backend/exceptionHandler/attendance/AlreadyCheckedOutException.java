package com.hrflow.hrflow_backend.exceptionHandler.attendance;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class AlreadyCheckedOutException extends BaseException {
    public AlreadyCheckedOutException(String message) {
        super(message, "ALREADY_CHECKED_OUT");
    }
}
