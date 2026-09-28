package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
//import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObLoanRepository extends JpaRepository<TbObLoan, String> {
    /** Most recent loan/BRE row first - application can have >1 BRE trigger history row. */
    List<TbObLoan> findByApplicationIdOrderByCreatedTsDesc(String applicationId);
    Optional<TbObLoan> findByApplicationIdAndCustomerId(String applicationId, String customerId);
    List<TbObLoan> findByCustomerIdIn(List<String> customerIds);
    Optional<TbObLoan> findByApplicationId(String applicationId);
    Optional<TbObLoan> findByLoanId(String loanId);
}