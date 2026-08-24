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
public class Folder {

	@JsonProperty("Comment")
	private String comment;

	@JsonProperty("Owner")
	private String owner;

	@JsonProperty("CreationDateTime")
	private String creationDateTime;

	@JsonProperty("ImageVolumeIndex")
	private String imageVolumeIndex;

	@JsonProperty("ParentFolderIndex")
	private String parentFolderIndex;

	@JsonProperty("FolderName")
	private String folderName;

	@JsonProperty("AccessType")
	private String accessType;

	@JsonProperty("FolderType")
	private String folderType;

	@JsonProperty("Location")
	private String location;
}
