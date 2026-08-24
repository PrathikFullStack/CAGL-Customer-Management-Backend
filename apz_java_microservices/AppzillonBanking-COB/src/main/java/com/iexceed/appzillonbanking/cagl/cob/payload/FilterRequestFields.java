package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FilterRequestFields {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("kmId")
    private String kmId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("mbdfId")
    private String mbdfId;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("validatedKycId")
    private String validatedKycId;

    @JsonProperty("registeredPhoneNumber")
    private String registeredPhoneNumber;

    @JsonProperty("pagination")
    private PaginationRequest pagination;
}