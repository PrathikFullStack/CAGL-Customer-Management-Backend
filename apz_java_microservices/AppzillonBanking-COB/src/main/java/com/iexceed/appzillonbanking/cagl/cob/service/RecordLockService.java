package com.iexceed.appzillonbanking.cagl.cob.service;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObRecordLock;
import com.iexceed.appzillonbanking.cagl.cob.exception.ApplicationLockedException;
import com.iexceed.appzillonbanking.cagl.cob.payload.LockInfoDto;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObRecordLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Implements the record-lock rule required by API 9:
 * <pre>
 * if tb_ob_record_lock row exists for applicationId
 *    and lock not expired (locked_ts + lock_duration_min > now)
 *    and locked_by != current user
 *       -> 423 LOCKED
 * Same user re-access extends the lock timer.
 * </pre>
 * PERMANENT locks (held by RPC Checker until final decision) never expire on
 * their own; they only end via explicit release.
 */
@Service
@RequiredArgsConstructor
public class RecordLockService {

    private final TbObRecordLockRepository recordLockRepository;

    public LockInfoDto evaluateAndMaybeExtend(String applicationId, String userId,
                                              String userRole, long lockDurationMinutes) {

        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(lockDurationMinutes));

        Optional<TbObRecordLock> won = recordLockRepository
                .acquireOrExtendOrTakeover(applicationId, userId, userRole, now, expiry);

        if (won.isPresent()) {
            TbObRecordLock lock = won.get();
            return LockInfoDto.builder()
                    .locked(true)
                    .lockedBy(lock.getLockedBy())
                    .lockedByRole(lock.getLockedByRole())
                    .lockType(lock.getLockType())
                    .lockExpiryTs(lock.getLockExpiryTs())   // now Instant — update LockInfoDto's field type too
                    .build();
        }

        TbObRecordLock currentHolder = recordLockRepository
                .findByApplicationIdAndStatus(applicationId, "ACTIVE")
                .orElseThrow(() -> new IllegalStateException(
                        "Lock conflict reported but no active lock found for " + applicationId));

        throw new ApplicationLockedException(
                applicationId,
                currentHolder.getLockedBy(),
                currentHolder.getLockedByRole(),
                currentHolder.getLockType(),
                currentHolder.getLockExpiryTs());
    }
}