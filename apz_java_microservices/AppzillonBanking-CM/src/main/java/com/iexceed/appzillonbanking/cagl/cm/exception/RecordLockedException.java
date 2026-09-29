package com.iexceed.appzillonbanking.cagl.cm.exception;

import lombok.Getter;

@Getter
public class RecordLockedException extends RuntimeException {
    private final String applicationId;
    private final String lockedBy;
    private final String lockedByRole;

    public RecordLockedException(String message) {
        super(message);
        this.applicationId = null;
        this.lockedBy = null;
        this.lockedByRole = null;
    }

    public RecordLockedException(String applicationId, String lockedBy, String lockedByRole) {
        super(String.format("Application '%s' is currently locked by user '%s' (%s)", applicationId, lockedBy, lockedByRole));
        this.applicationId = applicationId;
        this.lockedBy = lockedBy;
        this.lockedByRole = lockedByRole;
    }
}
