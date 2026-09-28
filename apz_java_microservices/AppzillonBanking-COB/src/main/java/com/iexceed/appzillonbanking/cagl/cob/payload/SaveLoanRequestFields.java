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
public class SaveLoanRequestFields {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("appVersion")
    private String appVersion;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("loanDtls")
    private LoanDtls loanDtls;
}