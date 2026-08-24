package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class FamilyMemberDto {
    private Long familyMemId;
    private String memberType;
    private String relation;
    private String name;
    private LocalDate dob;
    private String gender;
    private String mobileNum;
    private String kycType;
    private String kycDocId;
    private String kycDocFront;
    private String kycDocBack;
    private String photoDocId;
    private BigDecimal clarityScore;
    private String cbStatus;
    private Boolean isNominee;
    private Boolean isEarningMember;
    private Map<String, Object> nomineeBankDetails;
}
