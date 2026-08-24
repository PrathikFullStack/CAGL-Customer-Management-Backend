package com.iexceed.appzillonbanking.cagl.cob.exception;

public class RecordLockedException extends RuntimeException {
    private final String lockedBy;
    public RecordLockedException(String applicationId, String lockedBy) {
        super("Application " + applicationId + " is locked by " + lockedBy);
        this.lockedBy = lockedBy;
    }
    public String getLockedBy() {
        return lockedBy;
    }
}
