package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InvalidLeaveDatesException extends BaseException {
    public InvalidLeaveDatesException(String message) {
        super(message, "INVALID_LEAVE_DATES");
    }
}
