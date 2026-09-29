package com.iexceed.appzillonbanking.cagl.cm.service;

public interface RecordLockService {


    boolean acquireLock(String applicationId, String userId, String userRole);
    boolean releaseLock(String applicationId, String userId);
}
