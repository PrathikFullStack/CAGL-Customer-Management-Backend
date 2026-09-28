package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceDetails {

    @JsonProperty("member")
    private String member;

    @JsonProperty("Spouse")
    private String spouse;

    @JsonProperty("applicant_insurance_amt")
    private String applicantInsuranceAmt;

    @JsonProperty("spouse_insurance_amt")
    private String spouseInsuranceAmt;

    @JsonProperty("insuranceProvider")
    private String insuranceProvider;

    @JsonProperty("insurCharges")
    private String insurCharges;
}