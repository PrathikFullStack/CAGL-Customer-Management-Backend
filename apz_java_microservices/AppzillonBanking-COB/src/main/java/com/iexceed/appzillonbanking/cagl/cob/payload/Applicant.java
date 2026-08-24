package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Applicant {

    @JsonProperty("replacement_member_flag")
    private String replacementMemberFlag;

    @JsonProperty("hh_annual_income")
    private String hhAnnualIncome;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("loanType")
    private String loanType;

    @JsonProperty("spouse_insurance")
    private String spouseInsurance;

    @JsonProperty("losIndicator")
    private String losIndicator;

    @JsonProperty("activation_date")
    private String activationDate;

    @JsonProperty("loan_product_type")
    private String loanProductType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("product_code")
    private String product_code;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("slNo")
    private String slNo;

    @JsonProperty("DigiAgil_DFA_Flag")
    private String digiAgilDFAFlag;

    @JsonProperty("earning_flag")
    private String earningFlag;

    @JsonProperty("applicantType")
    private String applicantType;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("custId")
    private String custId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("durationOfAgreement")
    private String durationOfAgreement;

    @JsonProperty("bankProductId")
    private String bankProductId;

    @JsonProperty("applied_frequency")
    private String appliedFrequency;

    @JsonProperty("address")
    private List<AddressPayload> address;

    @JsonProperty("document")
    private List<DocumentPayload> document;

    @JsonProperty("kendra")
    private String kendra;

    @JsonProperty("losIndex")
    private String losIndex;

    @JsonProperty("applicant_insurance")
    private String applicantInsurance;

    @JsonProperty("custName")
    private String custName;

    @JsonProperty("depName")
    private String depName;

    @JsonProperty("loanAmount")
    private String loanAmount;

    @JsonProperty("enquiryType")
    private String enquiryType;

    @JsonProperty("spouse_insurance_amt")
    private String spouseInsuranceAmt;

    @JsonProperty("depType")
    private String depType;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("applicant_insurance_amt")
    private String applicantInsuranceAmt;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("village_name")
    private String villageName;

    @JsonProperty("applied_term_weeks")
    private String appliedTermWeeks;

    @JsonProperty("categoryId")
    private String categoryId;

    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("StateBranch")
    private String stateBranch;


}
