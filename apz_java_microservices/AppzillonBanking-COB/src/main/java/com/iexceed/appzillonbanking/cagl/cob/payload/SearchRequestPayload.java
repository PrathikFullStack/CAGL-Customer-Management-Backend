package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SearchRequestPayload {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("searchType")
    private String searchType;

    @JsonProperty("searchFields")
    private List<String> searchFields;

    @JsonProperty("searchValue")
    private Object searchValue;

    @JsonProperty("tileType")
    private String tileType;

    @JsonProperty("pagination")
    private PaginationRequest pagination;
}