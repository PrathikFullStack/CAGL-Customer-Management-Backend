package com.iexceed.appzillonbanking.kyc.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AadharAuthenticateRequestFields {

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("clientRefId")
    private String clientRefId;

    @JsonProperty("maskAadhaarNumber")
    private String maskAadhaarNumber;

}
