package com.iexceed.appzillonbanking.cagl.cob.payload;

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
public class TransferApplicationRequestObj {

    @JsonProperty("applicationDtls")
    private List<TransferApplicationDetails> applicationDtls;

    @JsonProperty("fromKendraId")
    private String fromKendraId;

    @JsonProperty("fromGroupId")
    private String fromGroupId;

    @JsonProperty("toKendraId")
    private String toKendraId;

    @JsonProperty("toGroupId")
    private String toGroupId;

    @JsonProperty("requestType")
    private String requestType; //group

    @JsonProperty("mappingReason")
    private String mappingReason;
}