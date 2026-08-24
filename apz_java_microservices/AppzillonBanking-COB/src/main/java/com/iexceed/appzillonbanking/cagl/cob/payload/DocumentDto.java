package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DocumentDto {
    private String docuId;
    private String category;
    private String subCat;
    private String kycType;
    private String legalDocName;
    private String legalDocId;
    private String dmsDocIdFront;
    private String dmsDocIdBack;
    private String photo;
    private Map<String, Object> ocrData;
    private Map<String, Object> inputData;
    private Boolean isEdited;
    private String editedBy;
    private List<String> editedFields;
    private BigDecimal clarityScore;
    private String clarityPass;
    private String dedupeStatus;
    private String validationStatus;
    private Integer docVersion;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
