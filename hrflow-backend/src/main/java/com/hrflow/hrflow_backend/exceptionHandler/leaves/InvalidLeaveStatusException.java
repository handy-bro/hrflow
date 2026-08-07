package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InvalidLeaveStatusException extends BaseException {
    public InvalidLeaveStatusException(String message) {
        super(message, "INVALID_LEAVE_STATUS");
    }
}
