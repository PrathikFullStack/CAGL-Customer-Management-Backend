package com.iexceed.appzillonbanking.cagl.incomeassesment.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementPayload {
    @JsonProperty("q1")
    private String q1;

    @JsonProperty("q2")
    private String q2;

    @JsonProperty("q3")
    private String q3;

    @JsonProperty("q4")
    private String q4;

    @JsonProperty("q5")
    private String q5;

    @JsonProperty("q6")
    private String q6;

    @JsonProperty("finalRisk")
    private String finalRisk;
    
    @JsonProperty("role")
    private String role;

	public boolean hasData() {
		boolean hasAnswers = q1 != null || q2 != null || q3 != null || q4 != null || q5 != null || q6 != null
				|| finalRisk != null;
		return role != null && !role.trim().isEmpty() && hasAnswers;
	}
}
