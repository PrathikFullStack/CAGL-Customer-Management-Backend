package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BMReinterviewKnowledgeAnswerRequestFields {

    @JsonProperty("questionId")
    private Long questionId;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("correct")
    private Boolean correct;
}
