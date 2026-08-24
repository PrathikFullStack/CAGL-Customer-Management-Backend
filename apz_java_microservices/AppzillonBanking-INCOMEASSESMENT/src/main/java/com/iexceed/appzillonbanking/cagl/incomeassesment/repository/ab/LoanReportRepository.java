package com.iexceed.appzillonbanking.cagl.incomeassesment.repository.ab;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.incomeassesment.domain.ab.LoanReportEntity;

@Repository
public interface LoanReportRepository extends JpaRepository<LoanReportEntity, String> {

    Optional<LoanReportEntity> findByApplicationId(String applicationId);

}