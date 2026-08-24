package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObRecordLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface TbObRecordLockRepository extends JpaRepository<TbObRecordLock, Long>, RecordLockCustomRepository {

    /**
     * Fetch the currently ACTIVE lock row (if any) for an application, with a
     * pessimistic write lock so concurrent fetch/extend calls for the same
     * application can't race each other while we evaluate/extend it.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rl from TbObRecordLock rl where rl.applicationId = :applicationId and rl.status = 'ACTIVE'")
    Optional<TbObRecordLock> findActiveLockForUpdate(@Param("applicationId") String applicationId);

    Optional<TbObRecordLock> findFirstByApplicationIdAndStatusOrderByLockedAtDesc(String applicationId, String status);

    default Optional<TbObRecordLock> findActiveLock(String applicationId) {
        return findFirstByApplicationIdAndStatusOrderByLockedAtDesc(applicationId, "ACTIVE");
    }

    Optional<TbObRecordLock> findByApplicationIdAndStatus(String applicationId, String active);

}
