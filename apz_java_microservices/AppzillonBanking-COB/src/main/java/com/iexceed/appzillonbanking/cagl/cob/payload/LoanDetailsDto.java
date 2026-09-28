package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record LoanDetailsDto(
        String loanSeqId,
        String applicationId,
        String customerId,
        String loanId,
        BigDecimal amount,
        BigDecimal approvedAmt,
        String loanStatus,
        String freq,
        String term,
        String product,
        Map<String, Object> productDetails,
        Map<String, Object> charges,
        Integer breTriggerPoint,
        String breRequestId,
        String breResponseStatus,
        String t24KendraId,
        String t24GroupId,
        String t24CustomerId,
        String addInfo,
        LocalDateTime createdTs,
        LocalDateTime updatedTs
) {
}