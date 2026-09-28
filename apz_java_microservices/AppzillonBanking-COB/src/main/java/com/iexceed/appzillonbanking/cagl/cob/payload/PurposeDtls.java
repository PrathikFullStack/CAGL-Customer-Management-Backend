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
public class PurposeDtls {

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("purpose")
    private String purpose;

    @JsonProperty("purposeDesc")
    private String purposeDesc;

    @JsonProperty("productSubPurpose")
    private String productSubPurpose;
}