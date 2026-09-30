package com.iexceed.appzillonbanking.cagl.cm.payload.workflow;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTransitionRequest {
    private String appId;
    private String applicationId;
    private String customerId;
    private String currentStage;
    private String action;
    private String remarks;
    private String initiatorRole;
    private Boolean isKycEdited;
    private Boolean isRpcEdited;
    private Boolean isAmlTriggered;
    private String breDecision;
    private Boolean isWidowedToMarried;
    private Map<String, Object> queryReasons;
}
