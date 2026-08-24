package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GRTLoanReviewRequestFields {

    @JsonProperty("requestedLoanAmount")
    private BigDecimal requestedLoanAmount;

    @JsonProperty("approvedLoanAmount")
    private BigDecimal approvedLoanAmount;

    @JsonProperty("tenure")
    private Integer tenure;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("insuranceAmount")
    private BigDecimal insuranceAmount;
}
