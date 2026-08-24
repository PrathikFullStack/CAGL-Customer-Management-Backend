package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerMisReportEntity;

@Repository
public interface CustomerMisReportRepository extends JpaRepository<CustomerMisReportEntity, String> {

	Optional<CustomerMisReportEntity> findByApplicationId(String applicationId);

}
