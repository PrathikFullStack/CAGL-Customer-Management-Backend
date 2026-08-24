package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RejectLeadRequestFields {

    @NotBlank(message = "leadId is required")
    @JsonProperty("leadId")
    private String leadId;

    @NotBlank(message = "rejectReason is required")
    @JsonProperty("rejectReason")
    private String rejectReason;
}
