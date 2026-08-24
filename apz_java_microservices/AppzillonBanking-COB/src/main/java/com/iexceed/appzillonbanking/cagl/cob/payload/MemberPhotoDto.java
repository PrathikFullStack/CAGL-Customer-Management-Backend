package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

import java.util.List;

@Builder
public record MemberPhotoDto(String status,
                             List<DocumentDetailsWrapper> documentList) {
    public MemberPhotoDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}
