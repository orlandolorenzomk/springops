package org.kreyzon.springops.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception class for SpringOps application.
 * This exception is used to represent application-specific errors
 * with an associated HTTP status code.
 * 
 * @author Domenico Ferraro
 */
public class SpringOpsException extends RuntimeException
{
    /**
     * The HTTP status associated with this exception.
     */
    private HttpStatus httpStatus;

    /**
     * Constructs a new SpringOpsException with the specified detail message
     * and HTTP status.
     * 
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param httpStatus the HTTP status associated with this exception.
     */
    public SpringOpsException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }


    /**
     * Retrieves the HTTP status associated with this exception.
     * 
     * @return the HTTP status.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
