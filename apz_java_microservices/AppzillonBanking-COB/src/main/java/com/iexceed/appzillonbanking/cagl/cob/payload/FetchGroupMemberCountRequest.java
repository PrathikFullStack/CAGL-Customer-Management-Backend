package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FetchGroupMemberCountRequest(@JsonProperty("interfaceName")
                                     String interfaceName,
                                           @JsonProperty("appId")
                                     String appId,
                                           @JsonProperty("userId")
                                     String userId,
                                           @JsonProperty("requestObj")
                                     FetchGroupMemberCountRequestFields requestObj) {
}
