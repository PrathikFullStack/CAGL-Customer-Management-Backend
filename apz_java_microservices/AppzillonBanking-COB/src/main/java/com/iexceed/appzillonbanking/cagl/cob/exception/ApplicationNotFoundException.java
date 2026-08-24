package com.iexceed.appzillonbanking.cagl.cob.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(String applicationId) {
        super("Application not found for applicationId: " + applicationId);
    }
}
