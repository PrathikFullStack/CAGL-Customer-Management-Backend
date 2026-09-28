package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DashboardListRequestFields {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tileType")
    private String tileType;

    @JsonProperty("subCategory")
    private String subCategory;

    @JsonProperty("flow")
    private String flow;

    @JsonProperty("pagination")
    private PaginationRequest pagination;

    @JsonProperty("caseAgingDays")
    private Integer caseAgingDays;

    @JsonProperty("ageing")
    private String ageing;

    public Integer getAgeingAsInt() {
        if (ageing == null || ageing.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(ageing.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @JsonProperty("kendraIds")
    private List<String> kendraIds;

    // PENDING_FOR_CGT tile: groupIds to check for cgtStatus="PENDING" directly, in place of
    // deriving groups from kendraIds.
    @JsonProperty("groupIds")
    private List<String> groupIds;

    @JsonProperty("branchIds")
    private List<String> branchIds;

    @JsonProperty("searchType")
    private String searchType;

    @JsonProperty("searchFields")
    private List<String> searchFields;

    @JsonProperty("searchValue")
    private Object searchValue;
}