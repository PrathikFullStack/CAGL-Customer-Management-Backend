package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Sub-stage 1.6 - kendra (center) assignment. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KendraSelectionDetails {

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("kendra_id")
    private String kendraId;

    @JsonProperty("kendra_name")
    private String kendraName;

    @JsonProperty("distancefromKendra")
    private String distanceFromKendra;
}
