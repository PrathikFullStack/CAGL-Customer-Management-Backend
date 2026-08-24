package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.util.List;

@Builder
public record FamilyDetailsDto(
        String maritalStatus,
        String status,
        List<FamilyMemberDetailsDto> memberList
) {
    public FamilyDetailsDto {
        memberList = memberList == null ? List.of() : List.copyOf(memberList);
    }
}