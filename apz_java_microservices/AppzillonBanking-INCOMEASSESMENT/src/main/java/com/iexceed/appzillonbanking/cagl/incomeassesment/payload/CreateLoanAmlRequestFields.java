package com.iexceed.appzillonbanking.cagl.incomeassesment.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLoanAmlRequestFields {
    private String applicationId;
    private String initiatedId;
    private String initiatedBy;
    private DisbursementPayload initiatedPayload; // questionid, answer
    private String disbId;
    private String disbBy;
    private DisbursementPayload disbPayload; // questionid, answer
    private String memberId;
    private String branchId;
    // initiated name, disbursement name, member id
}
