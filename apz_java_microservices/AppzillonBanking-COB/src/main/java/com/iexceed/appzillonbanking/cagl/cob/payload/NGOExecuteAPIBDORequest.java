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
public class NGOExecuteAPIBDORequest {

	@JsonProperty("NGOExecuteAPIBDO")
	private NGOExecuteAPIBDO ngoExecuteAPIBDO;
}
