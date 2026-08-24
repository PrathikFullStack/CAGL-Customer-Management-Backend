package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusFields {
	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("kendraIds")
	private List<String> kendraIds;

	@JsonProperty("branchId")
	private String branchId;

	@JsonProperty("seqNo")
	private Integer seqNo;

	@JsonProperty("payload")
	private Object payload;

	@JsonProperty("status")
	private String status;

	@JsonProperty("meetingDate")
	private String meetingDate;

}