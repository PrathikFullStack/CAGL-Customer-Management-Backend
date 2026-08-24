package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class BreSummaryDto {
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String loanStatus;
    private Integer lastBreTriggerPoint;
    private String breRequestId;
    private String breResponseStatus;
    private String productDetails;
    private String charges;
}
