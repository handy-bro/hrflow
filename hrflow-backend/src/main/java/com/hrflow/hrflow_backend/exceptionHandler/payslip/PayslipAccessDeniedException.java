package com.hrflow.hrflow_backend.exceptionHandler.payslip;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class PayslipAccessDeniedException extends BaseException {
    public PayslipAccessDeniedException(String message) {
        super(message, "PAYSLIP_ACCESS_DENIED");
    }
}
