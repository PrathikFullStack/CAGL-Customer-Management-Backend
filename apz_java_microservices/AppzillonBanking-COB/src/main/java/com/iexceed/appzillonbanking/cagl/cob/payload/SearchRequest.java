package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchRequest {
    @JsonProperty("reqObj")
    private SearchRequestPayload reqObj;
}