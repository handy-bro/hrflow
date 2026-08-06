package com.hrflow.hrflow_backend.exceptionHandler.attendance;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class AlreadyCheckedInException extends BaseException {
    public AlreadyCheckedInException(String message) {
        super(message, "ALREADY_CHECKED_IN");
    }
}
