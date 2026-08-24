package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GlobalSearchRequestFields {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("searchValue")
    private String searchValue;

    @JsonProperty("pagination")
    private PaginationRequest pagination;
}