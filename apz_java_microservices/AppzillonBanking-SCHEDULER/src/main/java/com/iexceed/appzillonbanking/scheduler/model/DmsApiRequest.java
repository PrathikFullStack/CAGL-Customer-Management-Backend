package com.iexceed.appzillonbanking.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DmsApiRequest {

	@JsonProperty("appId")
	private String appId;

	@JsonProperty("interfaceName")
	private String interfaceName;

	@JsonProperty("userId")
	private String userId;

	@JsonProperty("userRole")
	private String userRole;

	@JsonProperty("appVersion")
	private String appVersion;

	@JsonProperty("userName")
	private String userName;

	@JsonProperty("branchId")
	private String branchId;

	@JsonProperty("remarks")
	private String remarks;

	@JsonProperty("operationType")
	private String operationType;

	@JsonProperty("requestObj")
	private DmsRequestObj requestObj;

}
