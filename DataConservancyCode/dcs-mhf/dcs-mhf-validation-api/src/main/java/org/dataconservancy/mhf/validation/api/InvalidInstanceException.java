package org.dataconservancy.mhf.validation.api;

/**
 * Thrown to indicate that validation of a {@code MetadataInstance} failed.
 */
public class InvalidInstanceException extends ValidationException {

    public InvalidInstanceException() {
    }

    public InvalidInstanceException(Throwable cause) {
        super(cause);
    }

    public InvalidInstanceException(String message) {
        super(message);
    }

    public InvalidInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
