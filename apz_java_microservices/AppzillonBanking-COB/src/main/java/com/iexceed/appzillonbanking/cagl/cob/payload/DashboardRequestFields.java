package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DashboardRequestFields {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("tileType")
    private String tileType;

    @JsonProperty("subCategory")
    private String subCategory;

    @JsonProperty("pagination")
    private PaginationRequest pagination;

    @JsonProperty("caseAgingDays")
    private Integer caseAgingDays;

    @JsonProperty("kendraIds")
    private List<String> kendraIds;

    // PENDING_FOR_CGT tile count: groupIds to check for cgtStatus="PENDING" directly, in place of
    // deriving groups from kendraIds.
    @JsonProperty("groupIds")
    private List<String> groupIds;

    @JsonProperty("branchIds")
    private List<String> branchIds;
}