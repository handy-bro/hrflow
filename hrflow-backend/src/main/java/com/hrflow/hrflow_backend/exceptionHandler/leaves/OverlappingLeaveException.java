package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class OverlappingLeaveException extends BaseException {
    public OverlappingLeaveException(String message) {
        super(message, "OVERLAPPING_LEAVE");
    }
}
