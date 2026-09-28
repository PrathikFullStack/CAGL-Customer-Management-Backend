package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FetchGroupMemberCountRequestWrapper(
        @JsonProperty("apiRequest")
        FetchGroupMemberCountRequest apiRequest) {
}
