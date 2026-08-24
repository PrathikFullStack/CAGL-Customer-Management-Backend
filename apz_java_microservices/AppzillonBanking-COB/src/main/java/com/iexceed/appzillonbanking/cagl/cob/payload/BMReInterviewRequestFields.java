package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BMReInterviewRequestFields {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("locCaptureBy")
    private String locCaptureBy;

    @JsonProperty("custDetails")
    private BMReinterviewCustomerDetails custDetails;

    @JsonProperty("isDraft")
    private Boolean isDraft;

    @JsonProperty("subStage")
    private String subStage;

    @JsonProperty("subStageStatus")
    private String subStageStatus;

}
