package com.iexceed.appzillonbanking.cbs.repository.ab;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cbs.domain.ab.LoanReportEntity;
import jakarta.transaction.Transactional;

@Repository
public interface LoanReportRepository extends JpaRepository<LoanReportEntity, String> {

	LoanReportEntity findByApplicationId(String applicationId);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_ucob_loan_report " + "SET loanaccnum = :loanId, " + "status = 'DISBURSED' "
			+ "WHERE application_id = :applicationId", nativeQuery = true)
	int updateLoanIDAtLoanReportTable1(@Param("loanId") String loanId, @Param("applicationId") String applicationId);
	
	
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = """
			UPDATE tb_ucob_loan_report
			SET loanaccnum = :loanId,
			    status = 'DISBURSED'
			WHERE application_id = :applicationId
			""", nativeQuery = true)
	int updateLoanIDAtLoanReportTable(@Param("loanId") String loanId, @Param("applicationId") String applicationId);

}
