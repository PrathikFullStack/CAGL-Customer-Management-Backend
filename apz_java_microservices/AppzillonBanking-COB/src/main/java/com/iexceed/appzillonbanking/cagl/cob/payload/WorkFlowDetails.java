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
public class WorkFlowDetails {

    @JsonProperty("currentRole")
    private String currentRole;

    @JsonProperty("action")
    private String action;

    @JsonProperty("workflowId")
    private String workflowId;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("remarks")
    private String remarks;
}