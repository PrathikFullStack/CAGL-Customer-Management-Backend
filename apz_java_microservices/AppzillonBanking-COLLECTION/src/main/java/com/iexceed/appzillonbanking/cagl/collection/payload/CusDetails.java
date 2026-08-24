package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CusDetails {

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("cusCollectionAmt")
    private String cusCollectionAmt;

    @JsonProperty("cusFlag")
    private String cusFlag;

    @JsonProperty("upiFlag")
    private String upiFlag;

    @JsonProperty("loanDetails")
    private List<LoanDetails> loanDetails;


}
