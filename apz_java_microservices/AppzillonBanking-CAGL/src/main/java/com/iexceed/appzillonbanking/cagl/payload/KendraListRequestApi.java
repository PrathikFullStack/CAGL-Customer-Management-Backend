package com.iexceed.appzillonbanking.cagl.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KendraListRequestApi {

	@JsonProperty("interfaceName")
	private String interfaceName;

	@JsonProperty("requestObj")
	private KendraListRequestFields requestObj;

}
