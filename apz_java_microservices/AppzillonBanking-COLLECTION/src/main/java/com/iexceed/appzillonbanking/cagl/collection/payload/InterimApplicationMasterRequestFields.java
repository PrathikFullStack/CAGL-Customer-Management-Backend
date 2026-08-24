package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterimApplicationMasterRequestFields {

    private String appId;
    private String applicationId;
    private String latestVersionNo;
    private String kendraId;

    @JsonProperty("applicationDate")
    private LocalDate applicationDate;

    @JsonProperty("createTs")
    private LocalDateTime createTs;

    private String createdBy;
    private String applicationType;
    private String applicationStatus;
    private String branchCode;
    private String remarks;
    private String currentStage;
    private String kmid;
    private String kendraname;
    private String addInfo;
    private String payload;

}
