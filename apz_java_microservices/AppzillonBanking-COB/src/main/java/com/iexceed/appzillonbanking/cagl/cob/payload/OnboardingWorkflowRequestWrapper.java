package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingWorkflowRequestWrapper {

    @JsonProperty("apiRequest")
    private OnboardingWorkflowRequest apiRequest;
}
