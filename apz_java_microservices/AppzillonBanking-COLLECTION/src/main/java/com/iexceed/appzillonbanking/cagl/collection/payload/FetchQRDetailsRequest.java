package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchQRDetailsRequest {
    @JsonProperty("appId")
    private String appId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("requestObj")
    private FetchQRDetailsRequestFields requestObj;
}
