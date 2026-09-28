package com.iexceed.appzillonbanking.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DMSServiceResponse {

	private String applicationId;

	private String payload;

}
