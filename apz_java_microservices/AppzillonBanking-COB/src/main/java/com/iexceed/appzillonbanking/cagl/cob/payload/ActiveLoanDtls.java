package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveLoanDtls {

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("approvedAmt")
    private String approvedAmt;

    @JsonProperty("status")
    private String status;

    @JsonProperty("freq")
    private String freq;

    @JsonProperty("term")
    private String term;

    @JsonProperty("product")
    private String product;

    @JsonProperty("lnValueDate")
    private String lnValueDate;

    @JsonProperty("lnMatDate")
    private String lnMatDate;

    @JsonProperty("interestRate")
    private String interestRate;

    @JsonProperty("overduePrincipal")
    private String overduePrincipal;

    @JsonProperty("overdueInterest")
    private String overdueInterest;

    @JsonProperty("overDueStatus")
    private String overDueStatus;

    @JsonProperty("outstandingPrincipal")
    private String outstandingPrincipal;

    @JsonProperty("loanPurpose")
    private String loanPurpose;

    @JsonProperty("pf")
    private String pf;

    @JsonProperty("mem_insu")
    private String memInsu;

    @JsonProperty("sp_insu")
    private String spInsu;

    @JsonProperty("gst")
    private String gst;

    @JsonProperty("apr")
    private String apr;

    @JsonProperty("productType")
    private String productType;

    @JsonProperty("shortDesc")
    private String shortDesc;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("maturityDate")
    private String maturityDate;

    @JsonProperty("isCompulsory")
    private Boolean isCompulsory;
}