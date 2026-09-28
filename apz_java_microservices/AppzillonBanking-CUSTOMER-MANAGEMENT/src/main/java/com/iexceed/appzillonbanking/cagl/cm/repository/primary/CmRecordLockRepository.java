package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmRecordLockEntity;

@Repository
public interface CmRecordLockRepository extends JpaRepository<CmRecordLockEntity, String> {

    Optional<CmRecordLockEntity> findByApplicationIdAndStatus(String applicationId, String status);

    @Query("SELECT r FROM CmRecordLockEntity r WHERE r.applicationId = :appId AND r.status = 'ACTIVE' AND r.lockExpiryTs > :now")
    Optional<CmRecordLockEntity> findActiveLock(@Param("appId") String appId, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE CmRecordLockEntity r SET r.status = 'EXPIRED' WHERE r.status = 'ACTIVE' AND r.lockExpiryTs <= :now")
    int expireOldLocks(@Param("now") LocalDateTime now);
}
