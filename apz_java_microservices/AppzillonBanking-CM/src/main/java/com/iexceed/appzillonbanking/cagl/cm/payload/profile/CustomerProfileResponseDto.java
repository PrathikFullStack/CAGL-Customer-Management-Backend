package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponseDto {
    private ProfileHeaderCardDto header;
    private KycDetailsCardDto kycDetails;
    private AddressCardDto addresses;
    private BankDetailsCardDto bankDetails;
    private FamilyDetailsCardDto familyDetails;
    private IncomeAssessmentCardDto incomeAssessment;
    private ActiveLoansCardDto loansOverview;
    private List<RecentAuditChangeDto> recentChanges;
    private LockStatusDto lockStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockStatusDto {
        private boolean isLocked;
        private String lockedBy;
        private String lockedByRole;
        private String lockExpiry;
    }
}
