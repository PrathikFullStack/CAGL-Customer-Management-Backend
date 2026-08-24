package com.iexceed.appzillonbanking.cbs.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmlCheckRequestFields {

    @JsonProperty("applicantID")
    private String applicantID;

    @JsonProperty("applicantName")
    private String applicantName;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("primaryID")
    private String primaryID;

    @JsonProperty("branchID")
    private String branchID;

    @JsonProperty("branchName")
    private String branchName;

    @JsonProperty("customerID")
    private String customerID;

    @JsonProperty("groupID")
    private String groupID;

    @JsonProperty("kendraID")
    private String kendraID;

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("product")
    private String product;

    @JsonProperty("customerType")
    private String customerType;       // "GL" or "RF"

    @JsonProperty("reason")
    private String reason;             // default: "AML Check"

    @JsonProperty("uploadedBy")
    private String uploadedBy;
}
