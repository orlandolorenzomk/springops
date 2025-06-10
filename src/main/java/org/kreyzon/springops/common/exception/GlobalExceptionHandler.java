package org.kreyzon.springops.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.config.ApplicationConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Catches all exceptions and returns a JSON response with the error message.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ApplicationConfig applicationConfig;

    /**
     * Handles all exceptions and returns a JSON response with the error message.
     *
     * @param ex the exception that was thrown.
     * @return a {@link ResponseEntity} containing the error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        log.error("An error occurred: {}", ex.getMessage());
        errorResponse.put("error", ex.getMessage());

        if (applicationConfig.getDisplayExceptionStackTraces()) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}