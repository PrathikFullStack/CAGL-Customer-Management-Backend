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
public class DmsDocumentRequest {
	
	@JsonProperty("apiRequest")
	private DmsApiRequest apiRequest;

}
