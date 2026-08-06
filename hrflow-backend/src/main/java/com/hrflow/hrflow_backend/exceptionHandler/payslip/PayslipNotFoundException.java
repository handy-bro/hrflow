package com.hrflow.hrflow_backend.exceptionHandler.payslip;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class PayslipNotFoundException extends BaseException {
    public PayslipNotFoundException(String message) {
        super(message, "PAYSLIP_NOT_FOUND");
    }
}
