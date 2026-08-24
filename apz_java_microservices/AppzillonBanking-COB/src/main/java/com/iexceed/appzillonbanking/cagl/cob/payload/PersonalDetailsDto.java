package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.util.List;

@Builder
public record PersonalDetailsDto(
//        String addressType,
//        String commSameAsPerm,
        String nameSelected,
        String dobSelected,
        String pa,
        String ca,
        List<DocumentDetailsWrapper> documentList,
        String religion,
        String caste,
        String nationality,
        String email,
        String incomeSource
) {
    public PersonalDetailsDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}