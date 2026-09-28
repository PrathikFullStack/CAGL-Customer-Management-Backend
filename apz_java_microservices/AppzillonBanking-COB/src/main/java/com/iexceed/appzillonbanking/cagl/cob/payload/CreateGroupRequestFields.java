package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequestFields {

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("dmsFolderIdx")
    private String dmsFolderIdx;

    @JsonProperty("payload")
    private Map<String, Object> payload;

}
