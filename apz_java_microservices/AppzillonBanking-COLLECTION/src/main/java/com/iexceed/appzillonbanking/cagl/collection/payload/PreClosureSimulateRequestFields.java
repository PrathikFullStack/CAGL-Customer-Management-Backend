package com.iexceed.appzillonbanking.cagl.collection.payload;


import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreClosureSimulateRequestFields {

    @JsonProperty("arrangementId")
    private List<String> arrangementId;

    @JsonProperty("payoffDate")
    private String payoffDate;

    @JsonProperty("branchCode")
    private String branchCode;

    @JsonProperty("arrangementIdStr")
    private String arrangementIdStr;
}

