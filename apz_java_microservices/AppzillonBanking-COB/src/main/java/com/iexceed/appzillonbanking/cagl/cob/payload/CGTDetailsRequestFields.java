package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CGTDetailsRequestFields {

    @JsonProperty("cgtId")
    private Long cgtId;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("addCGTPayload")
    private List<CGTDayDetailsRequestFields> addCGTPayload;

    @JsonProperty("conductCGTPayload")
    private List<CGTDayDetailsRequestFields> conductCGTPayload;

    @JsonProperty("status")
    private String status;

    @JsonProperty("subStage")
    private List<Map<String, Object>> subStage;

    @JsonProperty("endCGTFlag")
    private Boolean endCGTFlag;

    @JsonProperty("isDraft")
    private Boolean isDraft;

}
