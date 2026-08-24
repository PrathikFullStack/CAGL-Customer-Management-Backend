package com.iexceed.appzillonbanking.cagl.collection.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClaimInitiationRequestFields {

    @JsonProperty("claimRequest")
    private List<ClaimRequest> claimRequest;

    @JsonProperty("status")
    private String status;
}
