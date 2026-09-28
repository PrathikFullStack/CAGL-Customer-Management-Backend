package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CGTMemberLoanDetailsFields {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("loanDetailsCaptured")
    private Boolean loanDetailsCaptured;
}
