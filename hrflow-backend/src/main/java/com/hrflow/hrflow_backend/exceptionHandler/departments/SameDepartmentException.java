package com.hrflow.hrflow_backend.exceptionHandler.departments;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class SameDepartmentException extends BaseException {
    public SameDepartmentException(String message) {
        super(message, "EMPLOYEE_ALREADY_IN_DEPARTMENT");
    }
}
