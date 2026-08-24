package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObOtherDocument;
import lombok.Builder;
import java.util.List;

@Builder
public record AdditionalDetailsDto(
        List<DocumentDetailsWrapper> documentList
) {
    public AdditionalDetailsDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}