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
public class DmsDocumenRequestFields {

	@JsonProperty("documentName")
	private String documentName;

	@JsonProperty("fileType")
	private String fileType;

	@JsonProperty("fileData")
	private String fileData;

	@JsonProperty("subType")
	private String subType;

	@JsonProperty("docIndex")
	private String docIndex;
}