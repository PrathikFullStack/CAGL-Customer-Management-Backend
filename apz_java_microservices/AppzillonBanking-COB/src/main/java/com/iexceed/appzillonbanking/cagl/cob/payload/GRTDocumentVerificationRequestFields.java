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
public class GRTDocumentVerificationRequestFields {

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("verified")
    private Boolean verified;

    @JsonProperty("remarks")
    private String remarks;
}
