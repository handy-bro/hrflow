package com.hrflow.hrflow_backend.exceptionHandler.leaves;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class UnauthorizedLeaveActionException extends BaseException {
    public UnauthorizedLeaveActionException(String message) {
        super(message, "UNAUTHORIZED_LEAVE");
    }
}
