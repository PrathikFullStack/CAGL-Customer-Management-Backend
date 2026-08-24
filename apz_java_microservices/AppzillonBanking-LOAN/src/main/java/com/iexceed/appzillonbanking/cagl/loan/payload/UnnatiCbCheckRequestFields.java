package com.iexceed.appzillonbanking.cagl.loan.payload;

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
public class UnnatiCbCheckRequestFields {

    @JsonProperty("Coapp_Insurance_flag")
    private String coappInsuranceFlag;

    @JsonProperty("App_Insurance_flag")
    private String appInsuranceFlag;

    @JsonProperty("App_Insurance_amt")
    private int appInsuranceAmt;

    @JsonProperty("Joint_Insurance_flag")
    private String jointInsuranceFlag;

    @JsonProperty("Coapp_flag")
    private int coappFlag;

    @JsonProperty("Coapp_Insurance_amt")
    private int coappInsuranceAmt;

    @JsonProperty("Joint_Insurance_amt")
    private int jointInsuranceAmt;

    @JsonProperty("caglOs")
    private String caglOs;

    @JsonProperty("roi_type")
    private int roiType;

    @JsonProperty("household_member")
    private List<HouseholdMemberPayload> householdMember;

    @JsonProperty("Q1") private String q1;
    @JsonProperty("Q2") private String q2;
    @JsonProperty("Q3") private String q3;
    @JsonProperty("Q4") private String q4;
    @JsonProperty("Q5") private String q5;
    @JsonProperty("Q6") private String q6;
    @JsonProperty("Q7") private String q7;
    @JsonProperty("Q8") private String q8;
    @JsonProperty("Q9") private String q9;
    @JsonProperty("Q10") private String q10;
    @JsonProperty("Q11") private String q11;
    @JsonProperty("Q12") private String q12;
    @JsonProperty("Q13") private String q13;
    @JsonProperty("Q14") private String q14;
    @JsonProperty("Q15") private String q15;
    @JsonProperty("Q16") private String q16;
    @JsonProperty("Q17") private String q17;
    @JsonProperty("Q18") private String q18;
    @JsonProperty("Q19") private String q19;
    @JsonProperty("Q20") private String q20;
    @JsonProperty("Q21") private String q21;
    @JsonProperty("Q22") private String q22;
    @JsonProperty("Q23") private String q23;
    @JsonProperty("Q24") private String q24;
    @JsonProperty("Q25") private String q25;
    @JsonProperty("Q26") private String q26;
    @JsonProperty("Q27") private String q27;
    @JsonProperty("Q28") private String q28;

    @JsonProperty("losIndicator")
    private String losIndicator;

    @JsonProperty("activation_date")
    private String activationDate;

    @JsonProperty("loan_product_type")
    private String loanProductType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("Field_Assessed_Income_of_customer")
    private String fieldAssessedIncomeOfCustomer;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("Income_Assessment_Flag")
    private String incomeAssessmentFlag;

    @JsonProperty("Self_Declared_Income_of_customer")
    private String selfDeclaredIncomeOfCustomer;

    @JsonProperty("durationOfAgreement")
    private String durationOfAgreement;

    @JsonProperty("bankProductId")
    private String bankProductId;

    @JsonProperty("losIndex")
    private String losIndex;

    @JsonProperty("depName")
    private String depName;

    @JsonProperty("enquiryType")
    private String enquiryType;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("applied_term_weeks")
    private Integer appliedTermWeeks;

    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("hh_annual_income")
    private long hhAnnualIncome;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("loanType")
    private String loanType;

    @JsonProperty("productCodeInternal")
    private String productCodeInternal;

    @JsonProperty("slNo")
    private String slNo;

    @JsonProperty("DigiAgil_DFA_Flag")
    private String digiAgilDfaFlag;

    @JsonProperty("earning_flag")
    private String earningFlag;

    @JsonProperty("applicantType")
    private String applicantType;

    @JsonProperty("custId")
    private String custId;

    @JsonProperty("term")
    private int term;

    @JsonProperty("email")
    private String email;

    @JsonProperty("applied_frequency")
    private String appliedFrequency;

    @JsonProperty("kendra")
    private String kendra;

    @JsonProperty("custName")
    private String custName;

    @JsonProperty("loanAmount")
    private String loanAmount;

    @JsonProperty("stateBranch")
    private String stateBranch;

    @JsonProperty("depType")
    private String depType;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("categoryId")
    private String categoryId;

    @JsonProperty("kendra_activation_date")
    private String kendraActivationDate;

    @JsonProperty("address")
    private List<AddressPayload> address;

    @JsonProperty("document")
    private List<DocumentPayload> document;


}
