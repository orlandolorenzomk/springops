package org.kreyzon.springops.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kreyzon.springops.config.ApplicationConfig;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Handles {@link HandlerMethodValidationException}, which occurs when a method argument validation fails.
     * Extracts field errors from the exception, constructs a flat map of field names to validation messages,
     * and returns them in the response body.
     *
     * Example response structure:
     *
     * {
     *   "name": "Name must not be blank",
     *   "email": "Email must be a valid format",
     *   "age": "Age must be greater than 18"
     * }
     *
     * @param ex the {@link HandlerMethodValidationException} that was thrown.
     * @return a {@link ResponseEntity} containing a map of field names to error messages and
     *         {@link HttpStatus#UNPROCESSABLE_ENTITY} as the status.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        Map<String, String> errors = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .collect(Collectors.toMap(
                        error -> {
                            String field = "unknown";
                            error.getCodes();
                            if (error.getCodes().length > 0) {
                                String[] parts = error.getCodes()[0].split("\\.");
                                field = parts[parts.length - 1]; // Get actual field name
                            }
                            return field;
                        },
                        MessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing // in case of duplicate field names
                ));

        log.info("Handler method validation errors occurred: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        log.info("Method argument validation errors occurred: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }



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