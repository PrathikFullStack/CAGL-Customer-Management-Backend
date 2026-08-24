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
public class BMReinterviewKnowledgeTestRequestFields {

    @JsonProperty("totalQuestions")
    private Integer totalQuestions;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("answers")
    private List<BMReinterviewKnowledgeAnswerRequestFields> answers;

    @JsonProperty("completedTs")
    private Long completedTs;
}
