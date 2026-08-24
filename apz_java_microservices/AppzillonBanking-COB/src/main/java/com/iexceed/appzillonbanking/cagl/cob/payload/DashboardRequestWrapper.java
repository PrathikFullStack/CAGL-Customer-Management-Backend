package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DashboardRequestWrapper {
    @JsonProperty("apiRequest")
    private DashboardRequest apiRequest;
}
