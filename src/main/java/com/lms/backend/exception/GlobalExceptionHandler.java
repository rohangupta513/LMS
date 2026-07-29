package com.lms.backend.exception;

import com.lms.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            jakarta.validation.ConstraintViolationException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            RuntimeException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequests(Exception ex, HttpServletRequest request) {
        if (ex instanceof MethodArgumentNotValidException || ex instanceof jakarta.validation.ConstraintViolationException) {
            Map<String, String> errors = new HashMap<>();
            if (ex instanceof MethodArgumentNotValidException) {
                ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().forEach((error) -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
            } else {
                ((jakarta.validation.ConstraintViolationException) ex).getConstraintViolations().forEach(violation ->
                        errors.put(violation.getPropertyPath().toString(), violation.getMessage())
                );
            }
            ErrorResponse response = ErrorResponse.withValidations(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Validation failed",
                    request.getRequestURI(),
                    errors
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericExceptions(Exception ex, HttpServletRequest request) {
        if (ex instanceof org.springframework.transaction.TransactionSystemException) {
            Throwable cause = ((org.springframework.transaction.TransactionSystemException) ex).getRootCause();
            if (cause instanceof jakarta.validation.ConstraintViolationException) {
                return handleBadRequests((Exception) cause, request);
            }
        }

        String message = "An unexpected error occurred: " + ex.getMessage();
        if (ex instanceof org.springframework.dao.DataAccessException || ex instanceof java.sql.SQLException) {
            message = "Database error occurred: " + ex.getMessage();
        } else if (ex instanceof org.springframework.transaction.TransactionSystemException) {
            message = "Database transaction error: " + ex.getMessage();
        }

        ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), message, request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
