package com.iexceed.appzillonbanking.cagl.cob.exception;

import lombok.Getter;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Thrown when an application is currently locked by a different user.
 * Mapped to HTTP 423 LOCKED by GlobalExceptionHandler.
 */
@Getter
public class ApplicationLockedException extends RuntimeException {

    private final String applicationId;
    private final String lockedBy;
    private final String lockedByRole;
    private final String lockType;
    private final Instant lockExpiryTs;

    public ApplicationLockedException(String applicationId, String lockedBy, String lockedByRole,
                                       String lockType, Instant lockExpiryTs) {
        super("Application " + applicationId + " is currently locked by user " + lockedBy);
        this.applicationId = applicationId;
        this.lockedBy = lockedBy;
        this.lockedByRole = lockedByRole;
        this.lockType = lockType;
        this.lockExpiryTs = lockExpiryTs;
    }
}
