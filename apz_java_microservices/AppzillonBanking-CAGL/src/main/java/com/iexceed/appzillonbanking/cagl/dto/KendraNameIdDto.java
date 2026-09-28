package com.iexceed.appzillonbanking.cagl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KendraNameIdDto {

	@JsonProperty("kendraId")
	private int kendraId;

	@JsonProperty("kendraName")
	private String kendraName;

}
