package com.hrflow.hrflow_backend.exceptionHandler;

import com.hrflow.hrflow_backend.exceptionHandler.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper to create JSON Headers
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyTakenException(
            EmailAlreadyTakenException ex, WebRequest request) {
        log.error("Email already taken: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({InvalidTokenException.class, ExpiredTokenException.class})
    public ResponseEntity<ErrorResponse> handleTokenException(
            BaseException ex, WebRequest request) {
        log.error("Token error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccountAlreadyVerifiedException.class})
    public ResponseEntity<ErrorResponse> handleAccountException(
            BaseException ex, WebRequest request) {
        log.error("Token error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({PasswordMismatchException.class})
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(
            BaseException ex, WebRequest request) {
        log.error("Password Mismatch error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }
}
