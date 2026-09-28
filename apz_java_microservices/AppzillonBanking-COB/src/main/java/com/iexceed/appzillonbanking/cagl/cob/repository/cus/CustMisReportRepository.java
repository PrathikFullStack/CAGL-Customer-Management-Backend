package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustMisReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustMisReportRepository
        extends JpaRepository<TbObCustMisReport, TbObCustMisReport.MisReportId> {
}
