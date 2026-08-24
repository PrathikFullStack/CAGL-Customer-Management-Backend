package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousePhotoRequestFields {

    @JsonProperty("housePhotoDocId")
    private String housePhotoDocId;

    @JsonProperty("clarityScore")
    private BigDecimal clarityScore;

    @JsonProperty("housePhotoClarityPass")
    private Character housePhotoClarityPass;

    @JsonProperty("housePhotoTs")  // House photo capture timestamp
    private Long housePhotoTs;

}
