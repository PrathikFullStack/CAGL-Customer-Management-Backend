package com.iexceed.appzillonbanking.cbs.payload;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequestField {

    @JsonProperty("recordId")
    private Long recordId;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("grtBy")
    private String grtBy;

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("groupType")
    private String groupType;

    @JsonProperty("kendraId")
    private Integer kendraId;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("grtDate")
    private String grtDate;

    @JsonProperty("referenceId")
    private String referenceId;

}
