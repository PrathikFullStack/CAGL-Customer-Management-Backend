package com.iexceed.appzillonbanking.cagl.loan.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberProfile {

	@JsonProperty("customerName")  
	private String customerName;
	
	@JsonProperty("groupId")
	private Long groupId;
	
	@JsonProperty("customerId")
	private String customerId;
	
	@JsonProperty("vintageYear")
	private String vintageYear;
	
	@JsonProperty("mobileNum")
	private String mobileNum;
	
	@JsonProperty("maritalStatus")
	private String maritalStatus;
	
	@JsonProperty("primaryType")
	private String primaryType;
	
	@JsonProperty("primaryId")
	private String primaryId;
	
	@JsonProperty("isEarning")
	private String isEarning;
	
	@JsonProperty("depname")
	private String depname;
	
	@JsonProperty("depDob")
	private String depDob;
	
	@JsonProperty("dob")
	private String dob;

}
