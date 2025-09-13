package dev.grantly.px.exception;

/**
 * Base exception class for all Grantly SDK related exceptions.
 * This provides a common base for all SDK-specific errors and allows
 * for centralized error handling in client applications.
 */
public class GrantlyException extends RuntimeException {
    
    /**
     * Constructs a new GrantlyException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public GrantlyException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new GrantlyException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the cause of this exception (which is saved for later retrieval)
     */
    public GrantlyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new GrantlyException with the specified cause.
     *
     * @param cause the cause of this exception (which is saved for later retrieval)
     */
    public GrantlyException(Throwable cause) {
        super(cause);
    }
}