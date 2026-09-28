package com.iexceed.appzillonbanking.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DmsHeader {

	private String userId;

	private String appId;

	private String interfaceId;

	private String deviceId;

	private String masterTxnRefNo;

}
