package com.iexceed.appzillonbanking.cagl.loan.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputDatas {

	@JsonProperty("name")
	private String name;

	@JsonProperty("dob")
	private String dob;

	@JsonProperty("legaldocName")
	private String legaldocName;

	@JsonProperty("legaldocId")
	private String legaldocId;

	@JsonProperty("memRelation")
	private String memRelation;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("mobileNum")
	private String mobileNum;
}
