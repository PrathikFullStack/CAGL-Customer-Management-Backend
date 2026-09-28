package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustAuditTrailRepository extends JpaRepository<TbObCustAuditTrail, String> {
    List<TbObCustAuditTrail> findByApplicationIdOrderByCreateTsDesc(String applicationId, Pageable pageable);

    List<TbObCustAuditTrail> findByApplicationId(String applicationId);

    @Query(value = "SELECT nextval('seq_ob_audit_trail_id')", nativeQuery = true)
    Long getNextAuditTrailId();

}
