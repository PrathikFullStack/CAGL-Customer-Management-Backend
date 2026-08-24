package com.iexceed.appzillonbanking.cagl.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadRequest {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("requestObj")
    private CreateLeadRequestFields requestObj;
}
