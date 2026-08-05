package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class InsufficientLeaveBalanceException extends BaseException {
    public InsufficientLeaveBalanceException(String message) {
        super(message, "INSUFFICIENT_LEAVE_BALANCE");
    }
}
