package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DocumentEntryDto(
        String docuId,
        String category,
        String subCat,
        String kycType,
        String authMode,
        String idType,
        String memRelation,
        String mappingId,
        String legalDocName,
        String legalDocId,
        String docuNoF,
        String docuNoB,
        String photo,
        String score,
        String status,
        String payload,
        String addInfo,
        Object ocrData,
        Object inputData,
        Boolean isEdited,
        String editedBy,
        List<String> editedFields,
        String reUploadedBy,
        String reason,
        BigDecimal clarityScore,
        Character clarityPass,
        String dedupeStatus,
        String validationStatus,
        int docVersion,
        Boolean isNominee,
        Boolean isEarningMember,
        Object nomineeBankDetails
) {
    public DocumentEntryDto {
        editedFields = editedFields == null ? List.of() : List.copyOf(editedFields);
    }
}