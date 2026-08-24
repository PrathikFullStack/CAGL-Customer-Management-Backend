package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingWorkflowResponse {

    private String status;
    private String nextRole;
    private String nextStageId;
    private String nextWorkflowStatus;
    private String errorCode;
    private String errorMessage;
    private String workflowId;
}
