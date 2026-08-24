package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObUserAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for TbObUserAuditTrail.
 */
@Repository
public interface TbObUserAuditTrailRepository extends JpaRepository<TbObUserAuditTrail, Long> {
    List<TbObUserAuditTrail> findByApplicationId(String applicationId);
    List<TbObUserAuditTrail> findByCustomerId(String customerId);

    // Get the latest audit trail entry for a specific user on a specific application
    @Query("SELECT MAX(a.createTs) FROM TbObUserAuditTrail a WHERE a.applicationId = :applicationId AND a.userId = :userId")
    Optional<LocalDateTime> findLastActivityByApplicationIdAndUserId(
            @Param("applicationId") String applicationId,
            @Param("userId") String userId);

}
