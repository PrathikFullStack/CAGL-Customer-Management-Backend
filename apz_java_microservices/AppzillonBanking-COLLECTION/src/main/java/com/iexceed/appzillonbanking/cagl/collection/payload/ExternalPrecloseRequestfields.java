package com.iexceed.appzillonbanking.cagl.collection.payload;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
    public class ExternalPrecloseRequestfields {

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

        @JsonProperty("branchCode")
        private String branchCode;

    }
