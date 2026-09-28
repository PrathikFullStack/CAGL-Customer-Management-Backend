package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Supports both a single application_id and a batch of them in the same shape -- the caller just
 * sends a one-element list for the single-record case, so there's no separate single/bulk contract
 * to maintain.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDedupeUpdateRequestFields {

    @JsonProperty("photoDedupeDetails")
    private List<PhotoDedupeStatusItem> photoDedupeDetails;
}
