package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmRecordLockEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmRecordLockRepository;

@Service
public class RecordLockService {

    private static final Logger logger = LogManager.getLogger(RecordLockService.class);
    private static final int LOCK_TIMEOUT_MINUTES = 15;

    private final CmRecordLockRepository lockRepo;

    public RecordLockService(CmRecordLockRepository lockRepo) {
        this.lockRepo = lockRepo;
    }

    /**
     * Attempts to acquire a pessimistic lock on an application
     */
    @Transactional("primaryTransactionManager")
    public boolean acquireLock(String applicationId, String userId, String userRole) {
        LocalDateTime now = LocalDateTime.now();

        // Expire older timeout locks
        lockRepo.expireOldLocks(now);

        Optional<CmRecordLockEntity> activeLock = lockRepo.findActiveLock(applicationId, now);
        if (activeLock.isPresent()) {
            CmRecordLockEntity lock = activeLock.get();
            if (lock.getLockedBy().equalsIgnoreCase(userId)) {
                // Refresh expiry for same user
                lock.setLockExpiryTs(now.plusMinutes(LOCK_TIMEOUT_MINUTES));
                lockRepo.save(lock);
                return true;
            }
            logger.warn("Application: {} is already locked by User: {}", applicationId, lock.getLockedBy());
            return false;
        }

        CmRecordLockEntity newLock = CmRecordLockEntity.builder()
                .lockId("LCK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .applicationId(applicationId)
                .lockedBy(userId)
                .lockedByRole(userRole)
                .lockType("EDIT")
                .lockedAt(now)
                .lockExpiryTs(now.plusMinutes(LOCK_TIMEOUT_MINUTES))
                .status("ACTIVE")
                .build();
        lockRepo.save(newLock);
        logger.info("Lock acquired successfully on App ID: {} by User: {}", applicationId, userId);
        return true;
    }

    /**
     * Releases active lock on application
     */
    @Transactional("primaryTransactionManager")
    public boolean releaseLock(String applicationId, String userId) {
        Optional<CmRecordLockEntity> activeLock = lockRepo.findByApplicationIdAndStatus(applicationId, "ACTIVE");
        if (activeLock.isPresent()) {
            CmRecordLockEntity lock = activeLock.get();
            lock.setStatus("RELEASED");
            lock.setReleasedAt(LocalDateTime.now());
            lockRepo.save(lock);
            logger.info("Lock released on App ID: {} by User: {}", applicationId, userId);
            return true;
        }
        return false;
    }
}
