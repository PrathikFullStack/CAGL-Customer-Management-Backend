package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingWorkflowRequest {

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("presentRole")
    private String  presentRole;

    @JsonProperty("requestObj")
    private WorkflowRequestFields requestObj;

}
