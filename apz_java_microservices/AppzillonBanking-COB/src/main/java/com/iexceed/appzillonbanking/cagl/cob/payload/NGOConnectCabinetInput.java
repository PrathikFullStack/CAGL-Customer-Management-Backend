package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NGOConnectCabinetInput {

	@JsonProperty("UserName")
	private String userName;

	@JsonProperty("CabinetName")
	private String cabinetName;

	@JsonProperty("UserPassword")
	private String userPassword;

	@JsonProperty("Option")
	private String option;

	@JsonProperty("locale")
	private String locale;

	@JsonProperty("UserExist")
	private String userExist;
}
