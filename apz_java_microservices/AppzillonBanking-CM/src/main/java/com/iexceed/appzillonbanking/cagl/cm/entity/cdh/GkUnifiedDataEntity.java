package com.iexceed.appzillonbanking.cagl.cm.entity.cdh;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "CUSTOMERID")
    private String customerId;

    @Column(name = "CUSTOMERNAME")
    private String customerName;

    @Column(name = "DOB")
    private String dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "MARITALSTATUS")
    private String maritalStatus;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    @Column(name = "PRIMARYID")
    private String primaryId;

    @Column(name = "PRIMARYTYPE")
    private String primaryType;

    @Column(name = "CUSTSTATUS")
    private String customerStatus;

    @Column(name = "CUST_QUALIFY")
    private String customerQualify;

    @Column(name = "cust_vintage")
    private String customerVintage;

    @Column(name = "activation_date")
    private String activationDate;

    // Hierarchy & Branch
    @Column(name = "BRANCH_ID")
    private String branchId;

    @Column(name = "BRANCHNAME")
    private String branchName;

    @Column(name = "kendraid")
    private Integer kendraId;

    @Column(name = "KENDRA_NAME")
    private String kendraName;

    @Column(name = "groupid")
    private Integer groupId;

    @Column(name = "KM_NAME")
    private String kmName;

    @Column(name = "LEADER_ID")
    private String leaderId;

    // Proximity & Location
    @Column(name = "HOUSE_LATITUDE", precision = 10, scale = 7)
    private BigDecimal houseLatitude;

    @Column(name = "HOUSE_LONGITUDE", precision = 10, scale = 7)
    private BigDecimal houseLongitude;

    @Column(name = "DISTANCE_FROM_BRANCH", precision = 6, scale = 2)
    private BigDecimal distanceFromBranch;

    // Permanent Address
    @Column(name = "PERMANENT_ADDRESS_LINE1")
    private String permanentAddressLine1;

    @Column(name = "PERMANENT_ADDRESS_LINE2")
    private String permanentAddressLine2;

    @Column(name = "PERMANENT_ADDRESS_LINE3")
    private String permanentAddressLine3;

    @Column(name = "PERMANENT_VILLAGE_LOCALITY")
    private String permanentVillageLocality;

    @Column(name = "PERMANENT_TALUK")
    private String permanentTaluk;

    @Column(name = "PERMANENT_DISTRICT")
    private String permanentDistrict;

    @Column(name = "PERMANENT_STATE")
    private String permanentState;

    @Column(name = "PERMANENT_PINCODE")
    private String permanentPincode;

    // Communication Address
    @Column(name = "COMMUNICATION_ADDRESS_LINE1")
    private String communicationAddressLine1;

    @Column(name = "COMMUNICATION_ADDRESS_LINE2")
    private String communicationAddressLine2;

    @Column(name = "COMMUNICATION_ADDRESS_LINE3")
    private String communicationAddressLine3;

    @Column(name = "COMMUNICATION_VILLAGE_LOCALITY")
    private String communicationVillageLocality;

    @Column(name = "COMMUNICATION_TALUK")
    private String communicationTaluk;

    @Column(name = "COMMUNICATION_DISTRICT")
    private String communicationDistrict;

    @Column(name = "COMMUNICATION_STATE")
    private String communicationState;

    @Column(name = "COMMUNICATION_PINCODE")
    private String communicationPincode;

    // Bank Details
    @Column(name = "BANKNAME")
    private String bankName;

    @Column(name = "BANKBRANCHNAME")
    private String bankBranchName;

    @Column(name = "BANKIFSCCODE")
    private String bankIfscCode;

    @Column(name = "BANKACNO")
    private String bankAccountNumber;

    @Column(name = "BANKACCOUNTNAME")
    private String bankAccountName;

    @Column(name = "BANK_VERIFICATION_STATUS")
    private String bankVerificationStatus;

    @Column(name = "BANK_VERIFIED_DATE")
    private String bankVerifiedDate;

    // Multi-KYC Dedicated Columns
    @Column(name = "AADHAAR_NUMBER_MASKED")
    private String aadhaarNumberMasked;

    @Column(name = "AADHAAR_NAME")
    private String aadhaarName;

    @Column(name = "AADHAAR_DOB")
    private String aadhaarDob;

    @Column(name = "PAN_NUMBER")
    private String panNumber;

    @Column(name = "PAN_NAME")
    private String panName;

    @Column(name = "PAN_FATHER_NAME")
    private String panFatherName;

    @Column(name = "PAN_DOB")
    private String panDob;

    @Column(name = "CKYC_ID")
    private String ckycId;

    @Column(name = "CKYC_CAPTURED_DATE")
    private String ckycCapturedDate;

    // Demographics & Land (for T24)
    @Column(name = "TITLE")
    private String title;

    @Column(name = "RELIGION")
    private String religion;

    @Column(name = "CASTE")
    private String caste;

    @Column(name = "NATIONALITY")
    private String nationality;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @Column(name = "NO_OF_ADULTS")
    private Integer noOfAdults;

    @Column(name = "NO_OF_CHILDREN")
    private Integer noOfChildren;

    @Column(name = "WET_LAND_ACRES", precision = 6, scale = 2)
    private BigDecimal wetLandAcres;

    @Column(name = "DRY_LAND_ACRES", precision = 6, scale = 2)
    private BigDecimal dryLandAcres;

    @Column(name = "SOURCE_OF_INCOME")
    private String sourceOfIncome;

    @Column(name = "DEVICE_TYPE")
    private String deviceType;

    @Column(name = "COMM_LANGUAGE")
    private String commLanguage;

    // Dependent / Spouse & Nominee Details
    @Column(name = "DEPNAME")
    private String depName;

    @Column(name = "DEPDOB")
    private String depDob;

    @Column(name = "DEPDOCTYPE")
    private String depDocType;

    @Column(name = "DEPDOCID")
    private String depDocId;

    @Column(name = "MEM_RELATION")
    private String memRelation;

    @Column(name = "SPOUSE_MOBILE_NUMBER")
    private String spouseMobileNumber;

    @Column(name = "SPOUSE_CKYC_ID")
    private String spouseCkycId;

    @Column(name = "NAME")
    private String nomineeName;

    @Column(name = "DOBE")
    private String nomineeDob;

    @Column(name = "MEM_RELATIONE")
    private String nomineeRelation;

    @Column(name = "LEGAL_DOC_NAME")
    private String nomineeDocType;

    @Column(name = "LEGAL_ID")
    private String nomineeDocId;

    @Column(name = "NOMINEE_BANK_DETAILS")
    private String nomineeBankDetails;

    // DMS Image IDs
    @Column(name = "CUST_PHOTO_DOC_ID")
    private String custPhotoDocId;

    @Column(name = "SPOUSE_PHOTO_DOC_ID")
    private String spousePhotoDocId;

    @Column(name = "ADDRESS_PROOF_DOC_ID")
    private String addressProofDocId;

    @Column(name = "PASSBOOK_DOC_ID")
    private String passbookDocId;

    // Active Loans & Financials
    @Column(name = "loan_id")
    private String loanId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "approved_amt")
    private String approvedAmount;

    @Column(name = "interest_rate")
    private String interestRate;

    @Column(name = "outstanding_principal")
    private String outstandingPrincipal;

    @Column(name = "overdue_status")
    private String overdueStatus;

    @Column(name = "product")
    private String product;

    @Column(name = "Eligible_CAGL_AMT")
    private Double eligibleCaglAmount;

    @Column(name = "Eligible_CAGL_Product")
    private String eligibleCaglProduct;

    @Column(name = "Overall_CB_Eligible_amount")
    private Double overallCbEligibleAmount;

    @Column(name = "TOT_EXPENSES")
    private String totalExpenses;

    @Column(name = "TOT_INCOME")
    private String totalIncome;
}
