package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GRTHouseVisitRequestFields {

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("amGPSInfo")
    private GPSCaptureRequestFields amGPSInfo;

    @JsonProperty("bmGPSInfo")
    private GPSCaptureRequestFields bmGPSInfo;

    @JsonProperty("kmGPSInfo")
    private GPSCaptureRequestFields kmGPSInfo;

    @JsonProperty("gpsDistanceAmBmKm")
    private BigDecimal gpsDistanceBmKm;

    @JsonProperty("gpsMisMatchFlag")
    private Character gpsMisMatchFlag;

    @JsonProperty("distanceFromKendra")
    private BigDecimal distanceFromKendra;

    @JsonProperty("distanceFromKendraFlag")
    private Character distanceFromKendraFlag;

    @JsonProperty("locationCapturedTs")   // GPS capture timestamp
    private Long locationCapturedTs;

    @JsonProperty("housePhoto")
    private HousePhotoRequestFields housePhoto;

    @JsonProperty("houseVisitVerificationFlag")
    private Boolean houseVisitVerificationFlag;
}
