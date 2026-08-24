package com.iexceed.appzillonbanking.cagl.repository.cus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.entity.CustomerInsuranceNotification;

@Repository
public interface CustInsNotificationRepository extends JpaRepository<CustomerInsuranceNotification, Long>{

	@Query("SELECT c FROM CustomerInsuranceNotification c WHERE c.customerId = :customerId")
	List<CustomerInsuranceNotification> fetchInsuranceDetailsByCustomerId(@Param("customerId") String customerId);

	// Check if customerId exists in insurance table
	boolean existsByCustomerId(String customerId);

}
