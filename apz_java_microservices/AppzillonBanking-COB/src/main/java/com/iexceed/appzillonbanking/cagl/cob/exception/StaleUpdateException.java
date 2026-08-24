package com.iexceed.appzillonbanking.cagl.cob.exception;

public class StaleUpdateException extends RuntimeException {
    public StaleUpdateException(String applicationId) {
        super("Application " + applicationId + " was modified by another user - refresh and retry");
    }
}
