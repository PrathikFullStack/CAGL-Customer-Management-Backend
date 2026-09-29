package com.iexceed.appzillonbanking.cagl.cm.service;

public interface RecordLockService {

    /**
     * Attempts to acquire a pessimistic lock on an application
     *
     * @param applicationId Application identifier
     * @param userId        Logged-in user ID
     * @param userRole      Logged-in role
     * @return true if lock was acquired or refreshed; false if locked by another user
     */
    boolean acquireLock(String applicationId, String userId, String userRole);

    /**
     * Releases active lock on application
     *
     * @param applicationId Application identifier
     * @param userId        Logged-in user ID
     * @return true if successfully released; false otherwise
     */
    boolean releaseLock(String applicationId, String userId);
}
