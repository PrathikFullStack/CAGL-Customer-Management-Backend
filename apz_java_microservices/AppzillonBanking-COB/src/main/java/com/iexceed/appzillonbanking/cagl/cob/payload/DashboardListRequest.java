package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class DashboardListRequest {

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("userId")
    private String userId;

    @Valid
    @JsonProperty("requestObj")
    private DashboardListRequestFields reqObj;
}