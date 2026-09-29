package com.iexceed.appzillonbanking.cagl.cm.payload.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
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
    private String breDecision; // PASS, FAIL, TIMEOUT
    private Boolean isWidowedToMarried;
    private Map<String, Object> queryReasons;
}
