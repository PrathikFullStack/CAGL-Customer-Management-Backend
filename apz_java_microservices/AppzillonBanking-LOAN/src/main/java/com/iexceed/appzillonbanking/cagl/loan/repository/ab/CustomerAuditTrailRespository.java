package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerAuditTrailEntity;

@Repository
public interface CustomerAuditTrailRespository extends JpaRepository<CustomerAuditTrailEntity, Integer> {

	@Query(value = "SELECT * FROM tb_ucob_customer_audit_trail c WHERE c.application_id =:applicationId  ORDER BY create_ts DESC LIMIT 1", nativeQuery = true)
	Optional<CustomerAuditTrailEntity> findTopByApplicationId(String applicationId);

	boolean existsByApplicationIdAndApplicationStatus(String applicationId, String applicationStatus);

	CustomerAuditTrailEntity findTopByApplicationIdOrderByCreateTsDesc(String applicationId);

}
