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
public class GRTQuestionnaireAnswerRequestFields {

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;
}
