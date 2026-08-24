package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/** Sub-stage 1.5 - household income / expense. */
@Builder
public record IncomeDet(String status,
                        IncomePayloadDto incomePayload,
                        List<QuestionPayloadDto> questPayload,
                        @JsonProperty("documentList")
                        List<DocumentListItem> documentList) {

    public record IncomePayloadDto(
            String status,
            @JsonProperty("questioncaptured")
            Boolean questionCaptured,
            String income,
            String expense
    ) {
    }

    public record QuestionPayloadDto(
            QuestionDto question
    ) {
    }
}
