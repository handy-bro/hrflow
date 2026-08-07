package com.hrflow.hrflow_backend.exceptionHandler.attendance;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class NotCheckedInException extends BaseException {
    public NotCheckedInException(String message) {
        super(message, "NOT_CHECKED_IN");
    }
}
