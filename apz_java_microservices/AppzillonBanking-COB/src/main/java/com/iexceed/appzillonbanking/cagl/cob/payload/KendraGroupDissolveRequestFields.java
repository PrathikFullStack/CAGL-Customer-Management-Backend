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
public class KendraGroupDissolveRequestFields {

    @JsonProperty("kendraIds")
    private List<String> kendraIds;

    @JsonProperty("groupIds")
    private List<String> groupIds;

    @JsonProperty("triggerType")
    private String triggerType;

    @JsonProperty("reason")
    private String reason;
}