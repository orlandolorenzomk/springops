package org.kreyzon.springops.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Catches all exceptions and returns a JSON response with the error message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all exceptions and returns a JSON response with the error message.
     *
     * @param ex the exception that was thrown.
     * @return a {@link ResponseEntity} containing the error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}