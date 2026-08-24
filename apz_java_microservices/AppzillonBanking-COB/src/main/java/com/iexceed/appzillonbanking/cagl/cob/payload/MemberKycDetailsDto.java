package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.util.List;

@Builder
public record MemberKycDetailsDto(
        List<DocumentDetailsWrapper> documentList,
        String ckycId
) {
    public MemberKycDetailsDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}