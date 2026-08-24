package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerNomineeDetails;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerNomineeDetailsId;

@Repository
public interface CustomerNomineeDetailsRepository extends JpaRepository<CustomerNomineeDetails, CustomerNomineeDetailsId> {

	@Query(value = "select * from tb_ucno_customer_nominee_details c WHERE c.application_id =:applicationId", nativeQuery = true)
	List<CustomerNomineeDetails> findNomineeDetailsBasedOnApplicationId(@Param("applicationId") String applicationId);

}
