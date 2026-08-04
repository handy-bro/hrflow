package com.hrflow.hrflow_backend.exceptionHandler;

public class EmployeeNotFoundException extends BaseException {
    public EmployeeNotFoundException(String message) {
        super(message, "EMPLOYEE_NOT_FOUND");
    }
}
