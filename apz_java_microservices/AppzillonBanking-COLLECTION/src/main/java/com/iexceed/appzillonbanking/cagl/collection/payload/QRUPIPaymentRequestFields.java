package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QRUPIPaymentRequestFields {
	
	@JsonProperty("customerId")
	private String customerId;
	
	@JsonProperty("customerName")
	private String customerName;

	@JsonProperty("amount")
	private String amount;

	@JsonProperty("billNumber")
	private String billNumber;

	@JsonProperty("remarks")
	private String remarks;

	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
	@JsonProperty("customerTxnId")
	private String customerTxnId;
	
	@JsonProperty("status")
	@JsonInclude(Include.NON_NULL)
	private String status;

	@JsonProperty("payload")
	private List<Object> payload;

	@JsonProperty("kendraId")
	private String kendraId;

	@JsonProperty("branchId")
	private String branchId;
}
