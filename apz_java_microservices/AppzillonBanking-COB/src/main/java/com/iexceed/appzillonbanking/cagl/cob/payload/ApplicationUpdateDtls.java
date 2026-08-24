package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUpdateDtls {

    @NotBlank(message = "application_id is required")
    @JsonProperty("application_id")
    private String applicationId;

    @NotBlank(message = "versionNum is required")
    @JsonProperty("versionNum")
    private String versionNum;

    @JsonProperty("stage")
    private String stage;

    @NotBlank(message = "sub_stage is required")
    @JsonProperty("sub_stage")
    private String subStage;

    @JsonProperty("wfstage")
    private String wfstage;

    @JsonProperty("km_name")
    private String kmName;

    @JsonProperty("channel_type")
    private String channelType;

    @JsonProperty("dmsFolderIdx")
    private String dmsFolderIdx;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("addInfo")
    private Map<String, Object> addInfo;

    @Valid
    @JsonProperty("customerDtls")
    private CustomerUpdateDtls customerDtls;
}
