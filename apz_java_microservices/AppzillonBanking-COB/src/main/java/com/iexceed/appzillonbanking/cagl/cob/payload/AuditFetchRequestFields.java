package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditFetchRequestFields {

    @NotBlank(message = "applicationId is required")
    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;
}
