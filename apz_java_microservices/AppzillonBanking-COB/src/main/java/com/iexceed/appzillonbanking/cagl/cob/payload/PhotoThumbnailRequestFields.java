package com.iexceed.appzillonbanking.cagl.cob.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoThumbnailRequestFields {

    @JsonProperty("applicationIds")
    private List<String> applicationIds;
    

    @JsonProperty("subType")
    private String subType;
}