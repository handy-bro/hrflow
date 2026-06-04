package com.hrflow.hrflow_backend.exceptionHandler;


import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final String code;
    public BaseException(String message, String code) {
        super(message);
        this.code = code;
    }
}
