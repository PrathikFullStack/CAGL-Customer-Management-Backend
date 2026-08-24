package com.iexceed.appzillonbanking.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoanDetails {

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("loanDue")
    private String loanDue;

    @JsonProperty("loanCollectionAmt")
    private String loanCollectionAmt;
}
