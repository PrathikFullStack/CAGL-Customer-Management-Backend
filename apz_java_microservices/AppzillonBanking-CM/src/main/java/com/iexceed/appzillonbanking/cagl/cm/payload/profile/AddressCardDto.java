package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCardDto {
    private AddressItemDto permanentAddress;
    private AddressItemDto communicationAddress;
    private boolean commSameAsPerm;
    private String distanceFromBranch;
    private String houseLatitude;
    private String houseLongitude;
    private String housePhotoDmsId;
    private String addressProofType;
    private String addressProofDocId;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressItemDto {
        private String line1;
        private String line2;
        private String line3;
        private String villageLocality;
        private String taluk;
        private String district;
        private String state;
        private String pincode;
    }
}
