package com.iexceed.appzillonbanking.cagl.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerInsuranceNotificationDto {

	private String customerId;
	private String customerName;
	private String kendraName;
	private String kendraId;
	private String branchName;
	private String branchId;

	private Map<String, List<String>> notifications;

}
