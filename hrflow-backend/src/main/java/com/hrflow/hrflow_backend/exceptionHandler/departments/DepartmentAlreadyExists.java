package com.hrflow.hrflow_backend.exceptionHandler.departments;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class DepartmentAlreadyExists extends BaseException {
    public DepartmentAlreadyExists(String message) {
        super(message, "DEPARTMENT_ALREADY_EXISTS");
    }
}
