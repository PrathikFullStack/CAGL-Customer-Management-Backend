package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdditionalApplicationData {
    String knowledgeTest;
    String gpsLat;
    String gpsLong;
    String housePhoto;

    public static AdditionalApplicationData empty() {
        return AdditionalApplicationData.builder()
                .knowledgeTest(null)
                .gpsLat(null)
                .gpsLong(null)
                .housePhoto(null)
                .build();
    }
}