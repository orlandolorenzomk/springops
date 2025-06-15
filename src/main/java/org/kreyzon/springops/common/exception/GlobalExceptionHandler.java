package org.kreyzon.springops.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kreyzon.springops.config.ApplicationConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    /**
     * Handles SpringOps specific exceptions and returns a JSON response with the error message.
     *
     * @param ex the SpringOps exception that was thrown.
     * @return a {@link ResponseEntity} containing the error message and HTTP status.
     * @author Domenico Ferraro
     */
    @ExceptionHandler(SpringOpsException.class)
    public ResponseEntity<Map<String, String>> handleSpringOpsException(SpringOpsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        log.info("SpringOps error occurred: {}", ex.getMessage());
        errorResponse.put("error", ex.getMessage());
        HttpStatus httpStatus = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;

        if (applicationConfig.getDisplayExceptionStackTraces()) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    /**
     * Handles UsernameNotFoundException and returns a JSON response with the error message.
     *
     * @param ex the UsernameNotFoundException that was thrown.
     * @return a {@link ResponseEntity} containing the error message and HTTP status.
     * @author Domenico Ferraro
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        log.info("Username not found: {}", ex.getMessage());
        errorResponse.put("error", ex.getMessage());

        if (applicationConfig.getDisplayExceptionStackTraces()) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ExpiredJwtException and returns a JSON response with the error message.
     *
     * @param ex the ExpiredJwtException that was thrown.
     * @return a {@link ResponseEntity} containing the error message and HTTP status.
     * @author Domenico Ferraro
     */
    @ExceptionHandler(GitAPIException.class)
    public ResponseEntity<Map<String, String>> handleGitAPIException(GitAPIException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        log.error("Git API error: {}", ex.getMessage());
        errorResponse.put("error", "Git error: " + ex.getMessage());

        if (applicationConfig.getDisplayExceptionStackTraces()) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }
}