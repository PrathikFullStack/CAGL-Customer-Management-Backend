package com.iexceed.appzillonbanking.scheduler.model;

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
public class MaitriUPIRequestFields {

    @JsonProperty("customerDetails")
    private List<CusDetails> customerDetails;

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @JsonProperty("branchCode")
    private String branchCode;
}
