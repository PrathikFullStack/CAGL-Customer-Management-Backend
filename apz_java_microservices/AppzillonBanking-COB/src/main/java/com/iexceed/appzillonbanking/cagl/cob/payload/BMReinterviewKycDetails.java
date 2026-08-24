package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BMReinterviewKycDetails {

    @JsonProperty("depName")
    private String depName;

    @JsonProperty("depRelationType")
    private String depRelationType;

    @JsonProperty("depDob")
    private String depDob;

    @JsonProperty("depDocType")
    private String depDocType;

    @JsonProperty("depDocId")
    private String depDocId;

    @JsonProperty("PA")
    private String PA;

    @JsonProperty("CA")
    private String CA;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("name")
    private String name;

    @JsonProperty("primaryType")
    private String primaryType;

    @JsonProperty("primaryId")
    private String primaryId;

    @JsonProperty("mobileNum")
    private String mobileNum;

    @JsonProperty("altMobileNum")
    private String altMobileNum;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("gpsLat")
    private String gpsLat;

    @JsonProperty("gpsLong")
    private String getLong;
}
