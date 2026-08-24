package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.core.payload.Response;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardListResponse extends Response {
    @JsonProperty("responseObj")
    private DashboardListResponseObj responseObj;

    public DashboardListResponse(DashboardListResponseObj responseObj) {
        this.responseObj = responseObj;
    }
}
