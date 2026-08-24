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
public class NGOExecuteAPIBDO {

	@JsonProperty("inputData")
	private InputData inputData;

	@JsonProperty("base64Encoded")
	private String base64Encoded;

	@JsonProperty("locale")
	private String locale;
}
