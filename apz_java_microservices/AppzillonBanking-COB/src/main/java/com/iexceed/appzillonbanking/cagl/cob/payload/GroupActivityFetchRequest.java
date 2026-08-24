package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupActivityFetchRequest {

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("requestObj")
    private GroupActivityFetchRequestFields requestObj;
}
