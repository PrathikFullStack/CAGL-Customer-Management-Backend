package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.util.List;

@Builder
public record IncomeDetailsDto(
        IncomeDet.IncomePayloadDto incomePayload,
        List<IncomeDet.QuestionPayloadDto> questPayload,
        List<DocumentDetailsWrapper> documentList
) {
    public IncomeDetailsDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}