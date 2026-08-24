package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.util.List;

@Builder
public record BankDetailsDto(
        String bankAccNo,
        String bankAccName,
        String bankBranchName,
        String bankName,
        String bankIfscCode,
        String status,
        Object pennyRes,
        List<DocumentDetailsWrapper> documentList
) {
    public BankDetailsDto {
        documentList = documentList == null ? List.of() : List.copyOf(documentList);
    }
}