package com.hrflow.hrflow_backend.exceptionHandler.storage;

import com.hrflow.hrflow_backend.exceptionHandler.BaseException;

public class StorageException extends BaseException {
    public StorageException(String message) {
        super(message, "STORAGE_EXCEPTION");
    }
}
