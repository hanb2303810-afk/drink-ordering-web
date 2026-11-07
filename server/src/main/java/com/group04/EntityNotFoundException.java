package com.group04;

/**
 * Unchecked exception, nem ra khi khong tim thay entity theo ID
 * trong Repository (vi du: khong tim thay Order, Drink).
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
