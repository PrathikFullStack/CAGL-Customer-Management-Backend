package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One application_id -> photo_dedupe_status pair. A single update request carries a list of these,
 * so a batch of application_ids can each be moved to a different status in one call.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDedupeStatusItem {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("photoDedupeStatus")
    private String photoDedupeStatus;
}
