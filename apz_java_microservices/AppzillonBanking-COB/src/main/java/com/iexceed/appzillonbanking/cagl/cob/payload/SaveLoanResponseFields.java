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
public class SaveLoanResponseFields {

    @JsonProperty("message")
    private String message;

    @JsonProperty("loanSeqId")
    private String loanSeqId;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("customerId")
    private String customerId;
}