package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FamilyMemberDetailsDto {
    private String memberType;                   // TbObFamilyMember.memberType -> SPOUSE/FATHER/EARNING/NOMINEE
    private String relationType;                 // TbObFamilyMember.relation -> "Spouse", "Son", ...
    private Boolean isEarning;                    // TbObFamilyMember.isEarningMember
    private Boolean isNominee;                    // TbObFamilyMember.isNominee
    private String mobileNum;
    private List<DocumentDetailsWrapper> documentList;  // resolved via kycDocId/kycDocFront/kycDocBack/photoDocId
}