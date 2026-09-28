package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbObCustAuditTrailRepository extends JpaRepository<TbObCustAuditTrail, Long> {
    List<TbObCustAuditTrail> findByApplicationIdOrderByCreateTsDesc(String applicationId, Pageable pageable);
    List<TbObCustAuditTrail> findByApplicationId(String applicationId);
    List<TbObCustAuditTrail> findByCustomerId(String customerId);


}