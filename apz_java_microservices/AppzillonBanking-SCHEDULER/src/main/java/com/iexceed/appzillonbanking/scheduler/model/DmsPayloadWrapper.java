package com.iexceed.appzillonbanking.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DmsPayloadWrapper{

    @JsonProperty("apiRequest")
    private DmsApiRequestWrapper apiRequest;

    @JsonProperty("header")
    private DmsHeader header;

    @JsonProperty("requestObj")
    private DmsDocumenRequestFields requestObj;
}