package com.hrflow.hrflow_backend.exceptionHandler.employees;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class EmployeeNotFoundException extends BaseException {
    public EmployeeNotFoundException(String message) {
        super(message, "EMPLOYEE_NOT_FOUND");
    }
}
