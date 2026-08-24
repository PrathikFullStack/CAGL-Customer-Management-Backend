package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BMReinterviewUnnatiQuestionnaireRequestFields {

    @JsonProperty("questionnaireAnswers")
    private List<BMReinterviewQuestionnaireAnswerRequestFields> answers;
}
