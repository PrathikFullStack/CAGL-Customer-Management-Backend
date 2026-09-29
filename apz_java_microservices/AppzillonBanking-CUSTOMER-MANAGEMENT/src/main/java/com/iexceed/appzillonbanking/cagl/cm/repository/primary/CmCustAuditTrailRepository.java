package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustAuditTrailEntity;

@Repository
public interface CmCustAuditTrailRepository extends JpaRepository<CmCustAuditTrailEntity, String> {

    List<CmCustAuditTrailEntity> findByApplicationIdOrderByCreateTsDesc(String applicationId);

    @Query(value = "SELECT * FROM tb_cm_cust_audit_trail WHERE customer_id = :customerId ORDER BY create_ts DESC LIMIT 3", nativeQuery = true)
    List<CmCustAuditTrailEntity> findLast3ChangesByCustomerId(@Param("customerId") String customerId);
}
