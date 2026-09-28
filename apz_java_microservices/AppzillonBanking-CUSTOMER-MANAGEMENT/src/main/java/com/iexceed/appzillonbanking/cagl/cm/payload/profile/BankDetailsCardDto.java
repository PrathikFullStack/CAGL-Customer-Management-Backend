package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsCardDto {
    private String bankName;
    private String bankBranchName;
    private String ifscCode;
    private String bankAccountNumberMasked;
    private String accountHolderName;
    private String bankVerificationStatus;
    private String bankVerifiedDate;
    private String passbookDocId;
}
