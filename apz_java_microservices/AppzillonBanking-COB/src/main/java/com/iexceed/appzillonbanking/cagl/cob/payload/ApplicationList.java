package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationList {

	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("versionNum")
	private Integer versionNum;

}
