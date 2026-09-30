package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import java.util.List;
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
public class FamilyDetailsCardDto {
    private SpouseDetailDto spouse;
    private NomineeDetailDto nominee;
    private List<FamilyMemberItemDto> familyMembers;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpouseDetailDto {
        private String name;
        private String dob;
        private String gender;
        private String kycType;
        private String kycDocId;
        private String mobileNumber;
        private String ckycId;
        private String photoDmsId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NomineeDetailDto {
        private String name;
        private String relation;
        private String dob;
        private String docType;
        private String docId;
        private NomineeBankDto bankDetails;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NomineeBankDto {
        private String bankName;
        private String accountNumber;
        private String ifscCode;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberItemDto {
        private String familyMemId;
        private String memberType;
        private String relation;
        private String name;
        private String dob;
        private String gender;
        private String mobileNum;
        private String kycType;
        private String kycDocId;
        private boolean isNominee;
        private boolean isEarningMember;
    }
}
