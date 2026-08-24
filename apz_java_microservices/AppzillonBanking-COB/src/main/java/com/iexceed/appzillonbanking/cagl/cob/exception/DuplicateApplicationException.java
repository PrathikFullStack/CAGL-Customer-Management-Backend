package com.iexceed.appzillonbanking.cagl.cob.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException() {
        super("Duplicate application found.");
    }

    public DuplicateApplicationException(String message) {
        super(message);
    }

    public DuplicateApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateApplicationException(Throwable cause) {
        super(cause);
    }
}