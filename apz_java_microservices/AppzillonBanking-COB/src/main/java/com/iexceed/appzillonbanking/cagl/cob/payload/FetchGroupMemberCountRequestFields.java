package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FetchGroupMemberCountRequestFields(
        @JsonProperty("userId")
        String userId,
        @JsonProperty("userRole")
        String userRole,
        @JsonProperty("branchId")
        String branchId
) {
}
