package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRequestFields {

    @JsonProperty("workflowId")
    private String workflowId;

    @JsonProperty("currentStage")
    private String currentStage;

    @JsonProperty("action")
    private String action;

    @JsonProperty("applicationId")
    private String applicationId;
}
