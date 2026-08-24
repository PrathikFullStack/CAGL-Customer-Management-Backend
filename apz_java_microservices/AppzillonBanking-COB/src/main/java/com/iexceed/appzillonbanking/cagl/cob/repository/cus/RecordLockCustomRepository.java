package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObRecordLock;

import java.time.Instant;
import java.util.Optional;

public interface RecordLockCustomRepository {

    Optional<TbObRecordLock> acquireOrExtendOrTakeover(
            String applicationId, String userId, String userRole, Instant now, Instant expiry);
}