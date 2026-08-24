package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateDeathClaimStatusRequestFields {
    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("status")
    private String status;
}
