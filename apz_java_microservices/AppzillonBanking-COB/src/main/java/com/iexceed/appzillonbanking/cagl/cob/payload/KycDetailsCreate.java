package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDetailsCreate {

    @JsonProperty("mobileNum")
    private String mobileNum;

    @JsonProperty("altMobileNum")
    private String altMobileNum;

    @JsonProperty("deviceType")
    private String deviceType;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("gpsLat")
    private String gpsLat;

    @JsonProperty("gpsLong")
    private String gpsLong;
}
