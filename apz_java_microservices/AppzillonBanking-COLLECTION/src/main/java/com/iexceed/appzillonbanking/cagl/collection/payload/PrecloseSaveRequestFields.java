package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrecloseSaveRequestFields {

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("branchCode")
    private String branchCode;

    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("loanAccount")
    private String loanAccount;

    @JsonProperty("creditCurrency")
    private String creditCurrency;

    @JsonProperty("depositAmount")
    private String depositAmount;

    @JsonProperty("valueDate")
    private String valueDate;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("meetingDate")
    private String meetingDate;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("productType")
    private String productType;

}
