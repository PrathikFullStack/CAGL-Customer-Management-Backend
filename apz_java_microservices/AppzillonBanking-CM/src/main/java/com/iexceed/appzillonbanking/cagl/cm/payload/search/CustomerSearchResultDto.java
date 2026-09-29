package com.iexceed.appzillonbanking.cagl.cm.payload.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchResultDto {
    private String customerId;
    private String customerName;
    private String mobileNumber;
    private String primaryKycType;
    private String primaryKycId;
    private String kendraId;
    private String kendraName;
    private String branchName;
    private String customerStatus;
    private String sourceSystem; // LOCAL_CM or CDH_REMOTE
    private String activeLoanCount;
    private String overdueStatus;
}
