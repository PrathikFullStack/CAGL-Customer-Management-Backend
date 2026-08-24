package com.iexceed.appzillonbanking.cagl.loan.payload;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
public class NomineeInsuranceResponseDto {

	private String appId;
	private String applicationId;
	private String versionNum;
	private String kendraId;

	private String memberId;
	private String loanId;

	private LocalDate applicationDate;
	private Timestamp createTs;

	private String createdBy;
	private String applicationType;
	private String kycType;
	private String applicationStatus;

	private String productGroupCode;
	private String productCode;
	private String branchCode;

	private String currentScreenId;
	private String remarks;
	private String cbCheck;
	private String currentStage;

	private String kmId;
	private String leader;
	private String kendraName;
	private String loanMode;

	private BigDecimal amount;
	private String customerName;

	private String nomineeName;
	private String nomineeApplicationId;

	private String cbApproveManual;

}