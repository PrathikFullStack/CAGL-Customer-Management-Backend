package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.LoanReportEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LoanReportRepository extends JpaRepository<LoanReportEntity, String> {

	LoanReportEntity findByApplicationId(String applicationId);
	LoanReportEntity findByLoanAccNum(String loanAccNum);
	@Query(value = "SELECT * FROM tb_ucob_loan_report " + "WHERE dbpayload LIKE CONCAT('%', :loanId, '%') " + "LIMIT 1", nativeQuery = true)
	LoanReportEntity findByLoanIdInDbPayload(@Param("loanId") String loanId);

}
