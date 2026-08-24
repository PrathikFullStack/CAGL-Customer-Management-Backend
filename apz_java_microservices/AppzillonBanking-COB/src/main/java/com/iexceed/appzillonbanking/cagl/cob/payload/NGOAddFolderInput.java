package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NGOAddFolderInput {

	@JsonProperty("UserDBId")
	private String userDBId;

	@JsonProperty("CabinetName")
	private String cabinetName;

	@JsonProperty("Option")
	private String option;

	@JsonProperty("Folder")
	private Folder folder;
}
