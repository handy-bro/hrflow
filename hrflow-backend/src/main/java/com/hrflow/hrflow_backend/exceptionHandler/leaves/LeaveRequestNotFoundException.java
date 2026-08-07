package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class LeaveRequestNotFoundException extends BaseException {
    public LeaveRequestNotFoundException(String message) {
        super(message, "LEAVE_REQUEST_NOT_FOUND");
    }
}
