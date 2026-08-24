package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<TbObLoan, Long> {
    /** Most recent loan/BRE row first - application can have >1 BRE trigger history row. */
    List<TbObLoan> findByApplicationIdOrderByCreatedTsDesc(String applicationId);
}
