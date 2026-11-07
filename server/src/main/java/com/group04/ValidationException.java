package com.group04;

/**
 * Unchecked exception, nem ra khi du lieu nghiep vu khong hop le
 * (vi du: don hang rong khong duoc phep gui di).
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
