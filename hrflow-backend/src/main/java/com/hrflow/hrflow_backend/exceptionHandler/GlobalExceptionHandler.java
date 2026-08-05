package com.hrflow.hrflow_backend.exceptionHandler;

import com.hrflow.hrflow_backend.exceptionHandler.auth.*;
import com.hrflow.hrflow_backend.exceptionHandler.departments.DepartmentNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.departments.SameDepartmentException;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.leaves.*;
import com.hrflow.hrflow_backend.exceptionHandler.response.ErrorResponse;
import com.hrflow.hrflow_backend.exceptionHandler.storage.StorageException;
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

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleRoleException(
            BaseException ex, WebRequest request) {
        log.error("Role error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(
            EmailAlreadyTakenException ex, WebRequest request) {
        log.error("Email not verified: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFoundException(
            DepartmentNotFoundException ex, WebRequest request) {
        log.error("Department not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageExceptionException(
            StorageException ex, WebRequest request) {
        log.error("Storage exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundExceptionException(
            EmployeeNotFoundException ex, WebRequest request) {
        log.error("Employee not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SameDepartmentException.class)
    public ResponseEntity<ErrorResponse> handleSameDepartmentException(
            SameDepartmentException ex, WebRequest request) {
        log.error("Employee already in this department: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientLeaveBalanceException(
            InsufficientLeaveBalanceException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidLeaveDatesException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLeaveDatesException(
            InvalidLeaveDatesException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidLeaveStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLeaveStatusException(
            InvalidLeaveStatusException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLeaveRequestNotFoundException(
            LeaveRequestNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OverlappingLeaveException.class)
    public ResponseEntity<ErrorResponse> handleOverlappingLeaveException(
            OverlappingLeaveException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedLeaveActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedLeaveActionException(
            UnauthorizedLeaveActionException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.toString(),
                ex.getCode(),
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, createJsonHeaders(), HttpStatus.UNAUTHORIZED);
    }
}
