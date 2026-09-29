package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDetailsCardDto {
    private String primaryKycType;
    private String primaryKycId;
    private String aadhaarMasked;
    private String aadhaarName;
    private String panNumber;
    private String panName;
    private String ckycId;
    private String ckycCapturedDate;
    private String kycStatus;
    private String kycValidationStatus;
    private String dmsDocIdFront;
    private String dmsDocIdBack;
    private String clarityScore;
    private String clarityPass;
    private String dedupeStatus;
}
