package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class QuestionDto {

    private final Map<String, String> questions = new LinkedHashMap<>();

    private String flow;

    @JsonAnySetter
    public void addQuestion(String key, Object value) {
        if ("flow".equals(key)) {
            this.flow = value != null ? value.toString() : null;
            return;
        }

        if (key.startsWith("q")) {
            questions.put(key, value != null ? value.toString() : null);
        }
    }

}