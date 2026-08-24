package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class AddressDto {
    private Long addressId;
    /** P = Permanent, C = Communication */
    private String addressType;
    private String commSameAsPerm;
    private String addrPayload;
    private String addressProofType;
    private String addressProofDocId;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private BigDecimal distanceFromBranch;
}
