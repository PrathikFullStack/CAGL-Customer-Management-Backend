package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObRecordLock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecordLockCustomRepositoryImpl implements RecordLockCustomRepository {

    private final EntityManager entityManager;

    private static final String ACQUIRE_OR_EXTEND_OR_TAKEOVER_SQL = """
            INSERT INTO tb_ob_record_lock
                (application_id, locked_by, locked_by_role, lock_type, status, locked_at, lock_expiry_ts)
            VALUES
                (:applicationId, :userId, :userRole, 'TIMED', 'ACTIVE', :now, :expiry)
            ON CONFLICT (application_id) WHERE status = 'ACTIVE'
            DO UPDATE SET
                locked_at      = :now,
                lock_expiry_ts = :expiry,
                locked_by      = :userId,
                locked_by_role = :userRole
            WHERE tb_ob_record_lock.locked_by = :userId
               OR tb_ob_record_lock.lock_expiry_ts <= :now
            RETURNING *
            """;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<TbObRecordLock> acquireOrExtendOrTakeover(
            String applicationId, String userId, String userRole, Instant now, Instant expiry) {

        try {
            @SuppressWarnings("unchecked")
            List<TbObRecordLock> result = entityManager
                    .createNativeQuery(ACQUIRE_OR_EXTEND_OR_TAKEOVER_SQL, TbObRecordLock.class)
                    .setParameter("applicationId", applicationId)
                    .setParameter("userId", userId)
                    .setParameter("userRole", userRole)
                    .setParameter("now", now)        // Hibernate 6 binds Instant -> timestamp natively
                    .setParameter("expiry", expiry)
                    .getResultList();
            System.out.println("result: " + result);

            return result.stream().findFirst();

        } catch (PersistenceException ex) {
            throw new IllegalStateException(
                    "Lock acquisition failed for application " + applicationId, ex);
        }
    }

}