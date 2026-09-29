package com.iexceed.appzillonbanking.cagl.cm.entity.cdh;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gk_unified_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GkUnifiedDataEntity {

    // 1. PRIMARY KEY & RECORD IDENTIFIERS
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "CUSTOMERID", nullable = false, length = 300)
    private String customerId;

    // 2. BRANCH & KENDRA HIERARCHY
    @Column(name = "BRANCH_ID", length = 20)
    private String branchId;

    @Column(name = "BRANCHNAME", length = 200)
    private String branchName;

    @Column(name = "kendraid")
    private Integer kendraId;

    @Column(name = "KENDRA_NAME", length = 100)
    private String kendraName;

    @Column(name = "groupid")
    private Integer groupId;

    @Column(name = "KM_NAME", length = 100)
    private String kmName;

    @Column(name = "LEADER_ID", length = 100)
    private String leaderId;

    // 3. CORE CUSTOMER PROFILE & DEMOGRAPHICS
    @Column(name = "TITLE", length = 20)
    private String title;

    @Column(name = "CUSTOMERNAME", columnDefinition = "TEXT")
    private String customerName;

    @Column(name = "gender", length = 300)
    private String gender;

    @Column(name = "DOB", length = 300)
    private String dob;

    @Column(name = "MARITALSTATUS", length = 300)
    private String maritalStatus;

    @Column(name = "CUSTSTATUS", length = 300)
    private String customerStatus;

    @Column(name = "cust_vintage", length = 5)
    private String customerVintage;

    @Column(name = "activation_date", length = 300)
    private String activationDate;

    @Column(name = "MOBILE_NUMBER", length = 300)
    private String mobileNumber;

    @Column(name = "l_MOBILE_NO_VAl", length = 300)
    private String altMobileNumber;

    @Column(name = "NO_OF_ADULTS")
    private Integer noOfAdults;

    @Column(name = "NO_OF_CHILDREN")
    private Integer noOfChildren;

    @Column(name = "DEVICE_TYPE", length = 50)
    private String deviceType;

    @Column(name = "COMM_LANGUAGE", length = 50)
    private String commLanguage;

    @Column(name = "CUST_PHOTO_DOC_ID", length = 100)
    private String custPhotoDocId;

    // 4. PRIMARY KYC (VOTER ID) & PERMANENT ADDRESS
    @Column(name = "PRIMARYTYPE", columnDefinition = "TEXT")
    private String primaryType;

    @Column(name = "PRIMARYID", columnDefinition = "TEXT")
    private String primaryId;

    @Column(name = "ADDRESS", columnDefinition = "TEXT")
    private String legacyAddress;

    @Column(name = "PERMANENT_ADDRESS_LINE1", columnDefinition = "TEXT")
    private String permanentAddressLine1;

    @Column(name = "PERMANENT_ADDRESS_LINE2", columnDefinition = "TEXT")
    private String permanentAddressLine2;

    @Column(name = "PERMANENT_ADDRESS_LINE3", columnDefinition = "TEXT")
    private String permanentAddressLine3;

    @Column(name = "PERMANENT_VILLAGE_LOCALITY", length = 300)
    private String permanentVillageLocality;

    @Column(name = "PERMANENT_TALUK", length = 300)
    private String permanentTaluk;

    @Column(name = "PERMANENT_DISTRICT", length = 300)
    private String permanentDistrict;

    @Column(name = "PERMANENT_STATE", length = 300)
    private String permanentState;

    @Column(name = "PERMANENT_PINCODE", length = 300)
    private String permanentPincode;

    // 5. SECONDARY & REGULATORY KYC (AADHAAR, PAN, CKYC)
    @Column(name = "AADHAAR_NUMBER_MASKED", length = 20)
    private String aadhaarNumberMasked;

    @Column(name = "AADHAAR_NAME", length = 300)
    private String aadhaarName;

    @Column(name = "AADHAAR_DOB", length = 50)
    private String aadhaarDob;

    @Column(name = "PAN_NUMBER", length = 20)
    private String panNumber;

    @Column(name = "PAN_NAME", length = 300)
    private String panName;

    @Column(name = "PAN_FATHER_NAME", length = 300)
    private String panFatherName;

    @Column(name = "PAN_DOB", length = 50)
    private String panDob;

    @Column(name = "CKYC_ID", length = 100)
    private String ckycId;

    @Column(name = "CKYC_CAPTURED_DATE", length = 50)
    private String ckycCapturedDate;

    // 6. COMMUNICATION ADDRESS & GPS GEO-LOCATION
    @Column(name = "COMMUNICATION_ADDRESS_LINE1", columnDefinition = "TEXT")
    private String communicationAddressLine1;

    @Column(name = "COMMUNICATION_ADDRESS_LINE2", columnDefinition = "TEXT")
    private String communicationAddressLine2;

    @Column(name = "COMMUNICATION_ADDRESS_LINE3", columnDefinition = "TEXT")
    private String communicationAddressLine3;

    @Column(name = "COMMUNICATION_VILLAGE_LOCALITY", length = 300)
    private String communicationVillageLocality;

    @Column(name = "COMMUNICATION_TALUK", length = 300)
    private String communicationTaluk;

    @Column(name = "COMMUNICATION_DISTRICT", length = 300)
    private String communicationDistrict;

    @Column(name = "COMMUNICATION_STATE", length = 300)
    private String communicationState;

    @Column(name = "COMMUNICATION_PINCODE", length = 300)
    private String communicationPincode;

    @Column(name = "HOUSE_LATITUDE", precision = 10, scale = 7)
    private BigDecimal houseLatitude;

    @Column(name = "HOUSE_LONGITUDE", precision = 10, scale = 7)
    private BigDecimal houseLongitude;

    @Column(name = "DISTANCE_FROM_BRANCH", precision = 6, scale = 2)
    private BigDecimal distanceFromBranch;

    @Column(name = "ADDRESS_PROOF_DOC_ID", length = 100)
    private String addressProofDocId;

    // 7. FAMILY & SPOUSE DETAILS
    @Column(name = "DEPNAME", columnDefinition = "TEXT")
    private String depName;

    @Column(name = "DEPDOB", length = 301)
    private String depDob;

    @Column(name = "DEPDOCTYPE", length = 300)
    private String depDocType;

    @Column(name = "DEPDOCID", length = 300)
    private String depDocId;

    @Column(name = "MEM_RELATION", length = 13)
    private String memRelation;

    @Column(name = "SPOUSE_MOBILE_NUMBER", length = 20)
    private String spouseMobileNumber;

    @Column(name = "SPOUSE_CKYC_ID", length = 100)
    private String spouseCkycId;

    @Column(name = "SPOUSE_PHOTO_DOC_ID", length = 100)
    private String spousePhotoDocId;

    // 8. EARNING MEMBERS & NOMINEES
    @Column(name = "REC_ID", length = 300)
    private String recId;

    @Column(name = "NAME", length = 300)
    private String nomineeName;

    @Column(name = "DOBE", length = 300)
    private String nomineeDob;

    @Column(name = "MEM_RELATIONE", length = 300)
    private String nomineeRelation;

    @Column(name = "LEGAL_DOC_NAME", length = 300)
    private String nomineeDocType;

    @Column(name = "LEGAL_ID", length = 300)
    private String nomineeDocId;

    @Column(name = "NOMINEE_BANK_DETAILS", length = 500)
    private String nomineeBankDetails;

    // 9. BANK ACCOUNT & VERIFICATION DETAILS
    @Column(name = "BANKACCOUNTNAME", length = 300)
    private String bankAccountName;

    @Column(name = "BANKACNO", length = 300)
    private String bankAccountNumber;

    @Column(name = "BANKIFSCCODE", length = 300)
    private String bankIfscCode;

    @Column(name = "BANKNAME", length = 300)
    private String bankName;

    @Column(name = "BANKBRANCHNAME", length = 300)
    private String bankBranchName;

    @Column(name = "BANK_VERIFICATION_STATUS", length = 50)
    private String bankVerificationStatus;

    @Column(name = "BANK_VERIFIED_DATE", length = 50)
    private String bankVerifiedDate;

    @Column(name = "PASSBOOK_DOC_ID", length = 100)
    private String passbookDocId;

    // 10. INCOME ASSESSMENT & QUALIFICATION
    @Column(name = "CUST_QUALIFY", length = 300)
    private String customerQualify;

    @Column(name = "TOT_INCOME", length = 300)
    private String totalIncome;

    @Column(name = "TOT_EXPENSES", length = 300)
    private String totalExpenses;

    @Column(name = "ASSESMENT_DATE", length = 300)
    private String assessmentDate;

    // 11. ADDITIONAL SOCIO-ECONOMIC DETAILS & LANDHOLDING
    @Column(name = "RELIGION", length = 50)
    private String religion;

    @Column(name = "CASTE", length = 50)
    private String caste;

    @Column(name = "NATIONALITY", length = 50)
    private String nationality;

    @Column(name = "EMAIL_ID", length = 200)
    private String emailId;

    @Column(name = "SOURCE_OF_INCOME", length = 100)
    private String sourceOfIncome;

    @Column(name = "WET_LAND_ACRES", precision = 6, scale = 2)
    private BigDecimal wetLandAcres;

    @Column(name = "DRY_LAND_ACRES", precision = 6, scale = 2)
    private BigDecimal dryLandAcres;

    // 12. LOANS, ELIGIBILITY & INSURANCE
    @Column(name = "loan_id", length = 300)
    private String loanId;

    @Column(name = "amount", length = 255)
    private String amount;

    @Column(name = "approved_amt", length = 300)
    private String approvedAmount;

    @Column(name = "freq", length = 300)
    private String frequency;

    @Column(name = "interest_rate", length = 300)
    private String interestRate;

    @Column(name = "ln_mat_date", length = 300)
    private String loanMaturityDate;

    @Column(name = "ln_Value_Date", length = 300)
    private String loanValueDate;

    @Column(name = "outstanding_principal", length = 255)
    private String outstandingPrincipal;

    @Column(name = "overdue_status", length = 255)
    private String overdueStatus;

    @Column(name = "overdue_interest", length = 255)
    private String overdueInterest;

    @Column(name = "overdue_principal", length = 255)
    private String overduePrincipal;

    @Column(name = "product", length = 30)
    private String product;

    @Column(name = "status", length = 300)
    private String status;

    @Column(name = "term", length = 20)
    private String term;

    @Column(name = "LoanPurpose", length = 35)
    private String loanPurpose;

    @Column(name = "pf", length = 300)
    private String pf;

    @Column(name = "GST", length = 300)
    private String gst;

    @Column(name = "mem_insu", length = 300)
    private String memberInsurance;

    @Column(name = "sp_insu", precision = 38, scale = 3)
    private BigDecimal spouseInsurance;

    @Column(name = "APR", length = 45)
    private String apr;

    @Column(name = "Eligible_CAGL_AMT")
    private Double eligibleCaglAmount;

    @Column(name = "Overall_CB_Eligible_amount")
    private Double overallCbEligibleAmount;

    @Column(name = "Eligible_CAGL_Product", length = 45)
    private String eligibleCaglProduct;

    @Column(name = "product_type", length = 45)
    private String productType;
}
