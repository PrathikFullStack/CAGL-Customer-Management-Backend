package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoThumbnailRequest {

	@JsonProperty("interfaceName")
	private String interfaceName;

	@JsonProperty("appId")
	private String appId;

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

    @JsonProperty("requestObj")
    private PhotoThumbnailRequestFields requestObj;
}
