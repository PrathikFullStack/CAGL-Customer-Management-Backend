package com.iexceed.appzillonbanking.cagl.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustData {
	
	private String customerId;
	private String customerName; 
	private int kendraId; 
	private int groupId; 
	//Used for @Unnati-FLag 
	private String groupName;
	private String branchName;
	private String primaryType; 
	private String primaryId; 
	private String dob; 
	private String maritalStatus; 
	private String address; 
	private String bankAccNo;
	private String bankAccName;
	private String bankBranchName;
	private String bankName; 
	private String bankIfscCode; 
	private String memRelation; 
	private String mobileNum; 
	private String permAddLine1; 
	private String permAddLine2;
	private String permanentState; 
	private String permanentDistrict;
	private String permanentVillageLocality;
	private String permanentPincode; 
	private String permanentTaluk;
	private String commAddLIne1;  
	private String commAddLIne2;  
	private String commState; 
	private String commDistrict; 
	private String commVillageLocality; 
	private String commPincode; 
	private String commTaluk;
	private String custVintage;
	private String activationDate;
	private String gender;
	private String depDocId;
	private String depDob;
	private String depDocType;
	private String depname;
	private String custStatus;
	private String custQualify;
	
	@JsonProperty("ins_notify")
	private String insNotify; // Surbhi 07-04-2026
	
	
	private List<LoanData> loanDtls;
	private List<LoanEligible> eligibleLoan;
	private List<IncomeAssesment> income;
	private List<CustEarnings> earnings;

	/**
	 * @author Ankit.CAG
	 */
	private List<MahiLeadDto> mahiLeads;
	
	private List<GkNomineeDTO> nomineeList;
	
}
