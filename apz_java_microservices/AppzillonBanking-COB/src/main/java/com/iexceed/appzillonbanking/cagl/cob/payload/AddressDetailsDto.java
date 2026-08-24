package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

@Builder
public record AddressDetailsDto(String addressId,
                                String customerId,
                                String applicationId,
                                String addressType,
                                String commSameAsPerm,
                                String addrPayload,
                                String addressProofType,
                                String addressProofDocId,
                                String distanceFromBranch,
                                String createdTs,
                                String updatedTs) {
}
