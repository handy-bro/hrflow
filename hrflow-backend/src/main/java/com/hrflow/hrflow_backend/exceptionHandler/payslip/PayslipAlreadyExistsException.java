package com.hrflow.hrflow_backend.exceptionHandler.payslip;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class PayslipAlreadyExistsException extends BaseException {
    public PayslipAlreadyExistsException(String message) {
        super(message, "PAYSLIP_ALREADY_EXISTS");
    }
}
