package com.iexceed.appzillonbanking.cbs.repository.ab;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cbs.domain.ab.CustomerApplicationMasterEntity;
import com.iexceed.appzillonbanking.cbs.domain.ab.CustomerApplicationMasterId;

import jakarta.transaction.Transactional;

@Repository
public interface CustomerApplicationMasterRepository extends CrudRepository<CustomerApplicationMasterEntity, CustomerApplicationMasterId> {

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_ucao_customer_application_master " + "SET loan_application_no = :applicationId, "
			+ "loan_id = :loanId " + "WHERE application_id = :nomineeApplicationID", nativeQuery = true)
	int updateLoanAppIDAndLoanId(@Param("applicationId") String applicationId, @Param("loanId") String loanId,
			@Param("nomineeApplicationID") String nomineeApplicationID);
}