package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * pageNumber is 0-based. Both fields are optional and pagination is opt-in: if neither is supplied,
 * the entire pending backlog is returned unpaged. Supplying either one turns pagination on, with the
 * other field falling back to a default (0 / 10); pageSize is capped once pagination is active so a
 * caller can't pull the whole backlog at once through an oversized page.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDedupeFetchRequestFields {

    @JsonProperty("pageNumber")
    private Integer pageNumber;

    @JsonProperty("pageSize")
    private Integer pageSize;
}
