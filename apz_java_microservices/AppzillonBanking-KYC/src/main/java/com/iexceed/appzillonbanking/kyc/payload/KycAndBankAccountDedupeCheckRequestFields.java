package com.iexceed.appzillonbanking.kyc.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycAndBankAccountDedupeCheckRequestFields {

    @JsonProperty("transId")
    private String transId;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("senderId")
    private String senderId;

    @JsonProperty("customerid")
    private String customerId;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("actiontypes")
    private String actionTypes;
}
