package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

@Builder
public record BreCheckDto(
        String loanAmount,
        String overDueAmount,
        String writtenoffAmount,
        String indebtedness,
        String foirAmount,
        String breStatus,
        String finalFior,
        String breDate,
        String reason,
        String product,
        String questionnaire
) {}