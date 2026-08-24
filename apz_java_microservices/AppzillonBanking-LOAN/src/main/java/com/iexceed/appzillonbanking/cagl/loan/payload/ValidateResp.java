package com.iexceed.appzillonbanking.cagl.loan.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateResp {
	
	@JsonProperty("name")
	private String name;

	@JsonProperty("dob")
	private String dob;

	@JsonProperty("legaldocName")
	private String legaldocName;

	@JsonProperty("legaldocId")
	private String legaldocId;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("mobileNum")
	private String mobileNum;
	
	@JsonProperty("memRelation")
	private String memRelation;

}
