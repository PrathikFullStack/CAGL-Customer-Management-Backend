package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FetchCustomerTxnDetailsRequestWrapper {
    @JsonProperty("apiRequest")
    private FetchCustomerTxnDetailsRequest apiRequest;
}
