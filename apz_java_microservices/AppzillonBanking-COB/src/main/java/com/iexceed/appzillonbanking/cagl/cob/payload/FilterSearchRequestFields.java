package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilterSearchRequestFields {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("searchType")
    private List<String> searchType;

    @JsonProperty("searchValue")
    private List<Map<String, List<String>>> searchValue;

    @JsonProperty("pagination")
    private PaginationRequest pagination;

    public String getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getBranchId() {
        return branchId;
    }
}
