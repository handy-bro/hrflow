package com.hrflow.hrflow_backend.exceptionHandler;

public class DepartmentNotFoundException extends BaseException {
    public DepartmentNotFoundException(String message) {
        super(message, "DEPARTMENT_NOT_FOUND");
    }
}
