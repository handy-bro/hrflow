package com.hrflow.hrflow_backend.exceptionHandler.departments;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class DepartmentNotFoundException extends BaseException {
    public DepartmentNotFoundException(String message) {
        super(message, "DEPARTMENT_NOT_FOUND");
    }
}
