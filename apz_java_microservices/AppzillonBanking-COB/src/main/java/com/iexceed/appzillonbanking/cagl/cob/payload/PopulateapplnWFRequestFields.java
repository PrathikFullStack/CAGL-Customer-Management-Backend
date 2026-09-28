package com.iexceed.appzillonbanking.cagl.cob.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopulateapplnWFRequestFields {
	
	@JsonProperty("applicationDetailList")
	private List<ApplicationList> applicationDetailList;

	@JsonProperty("workflow")
	private WorkFlowDetails workflow;

	@JsonProperty("createdBy")
	private String createdBy;

	@JsonProperty("appId")
	private String appId;
	
	@JsonProperty("userRole")
	private String userRole;
	
	@JsonProperty("userName")
	private String userName;
	
}
