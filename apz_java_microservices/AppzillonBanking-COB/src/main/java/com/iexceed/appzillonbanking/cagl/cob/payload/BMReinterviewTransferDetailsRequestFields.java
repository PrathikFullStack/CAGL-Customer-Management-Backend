package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BMReinterviewTransferDetailsRequestFields {

    @JsonProperty("fromKendra")
    private String fromKendra;

    @JsonProperty("fromGroup")
    private String fromGroup;

    @JsonProperty("toKendra")
    private String toKendra;

    @JsonProperty("toGroup")
    private String toGroup;

    @JsonProperty("timestamp")
    private Long timestamp;
}
