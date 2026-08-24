package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateDeathClaimStatusRequestWrapper {
    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("apiRequest")
    private UpdateDeathClaimStatusRequest apiRequest;
}
