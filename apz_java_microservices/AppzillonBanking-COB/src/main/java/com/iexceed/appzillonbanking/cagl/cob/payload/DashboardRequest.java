package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class DashboardRequest {
    @Valid
    @JsonProperty("requestObj")
    private DashboardRequestFields reqObj;
}
