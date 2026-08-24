package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record CustomerDetailsDto(String mobileNum,String alterMobileNum,
                                 String language, String deviceType,
                                 String customerId,
                                 String photoDocId,
                                 Map<String, Object> kycDetails,
                                 MemberPhotoDto memberPhoto,
                                 MemberKycDetailsDto memberKycDetails,
                                 PersonalDetailsDto personalAddressDetails,
                                 FamilyDetailsDto familyDetails,
                                 IncomeDetailsDto incomeDet,
                                 BankDetailsDto bankDet,
                                 AdditionalDetailsDto additionalDocuDet,
                                 Map<String, Object> verificationDet) {
}
