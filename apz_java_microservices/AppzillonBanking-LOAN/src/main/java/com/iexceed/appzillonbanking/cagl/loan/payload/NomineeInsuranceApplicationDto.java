package com.iexceed.appzillonbanking.cagl.loan.payload;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public interface NomineeInsuranceApplicationDto {

    String getAppId();
    String getApplicationId();
    String getVersionNum();
    String getKendraId();
    String getCustomerId();

    LocalDate getApplicationDate();
    Timestamp getCreateTs();

    String getCreatedBy();
    String getApplicationType();
    String getKycType();
    String getApplicationStatus();

    String getProductGroupCode();
    String getProductCode();
    String getBranchCode();

    String getCurrentScreenId();
    String getRemarks();
    String getCbCheck();
    String getCurrentStage();

    String getKmId();
    String getLeader();
    String getKendraName();
    String getLoanMode();

    BigDecimal getAmount();
    String getCustomerName();
    String getAddInfo();

    String getApplicationRefNo();
    String getCbApproveManual();
}