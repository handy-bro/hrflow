package com.hrflow.hrflow_backend.exceptionHandler;

public class StorageException extends BaseException {
    public StorageException(String message) {
        super(message, "STORAGE_EXCEPTION");
    }
}
