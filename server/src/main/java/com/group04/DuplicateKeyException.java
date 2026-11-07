package com.group04;

/**
 * Unchecked exception, nem ra khi co gang tao moi mot entity
 * voi ID da ton tai trong Repository.
 */
public class DuplicateKeyException extends RuntimeException {

    public DuplicateKeyException(String message) {
        super(message);
    }
}
