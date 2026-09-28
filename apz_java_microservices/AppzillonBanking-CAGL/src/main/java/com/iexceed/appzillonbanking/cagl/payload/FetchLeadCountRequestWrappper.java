package com.iexceed.appzillonbanking.cagl.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FetchLeadCountRequestWrappper(
        @JsonProperty("apiRequest")
        FetchLeadCountRequest apiRequest) {
}
