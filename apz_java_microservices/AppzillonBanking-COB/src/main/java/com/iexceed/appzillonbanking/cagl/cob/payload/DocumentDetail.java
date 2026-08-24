package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Mirrors the "documentDetails" shape repeated throughout the update payload
 * (memberPhoto, memberKycDetails.documentList[].documentDetails,
 * personalAddressDet.documentDetails[], familDetails.memberList[].documentList[].documentDetails,
 * incomDet.documentDetails[], bankDet.documentDetails[], AdditionalDocuDet.documentDetails[]).
 * One class covers all of them since the client sends the same envelope for every document type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetail {

    @JsonProperty("docuId")
    private String docuId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("subCat")
    private String subCat;

    @JsonProperty("kycType")
    private String kycType;

    @JsonProperty("authMode")
    private String authMode;

    @JsonProperty("idType")
    private String idType;

    @JsonProperty("payload")
    private JsonNode payload;

    @JsonProperty("memRelation")
    private String memRelation;

    @JsonProperty("legalDocName")
    private String legalDocName;

    @JsonProperty("legalDocId")
    private String legalDocId;

    @JsonProperty("ocrData")
    private Map<String, Object> ocrData;

    @JsonProperty("inputData")
    private Map<String, Object> inputData;

    @JsonProperty("docuNoF")
    private String docuNoF;

    @JsonProperty("docuNoB")
    private String docuNoB;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("isEdited")
    private Boolean isEdited;

    @JsonProperty("editedBy")
    private String editedBy;

    @JsonProperty("editedFields")
    private List<String> editedFields;

    @JsonProperty("reUploadedBy")
    private String reUploadedBy;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("clarityScore")
    private BigDecimal clarityScore;

    @JsonProperty("clarityPass")
    private Character clarityPass;

    @JsonProperty("dedupeStatus")
    private String dedupeStatus;

    @JsonProperty("validationStatus")
    private String validationStatus;

    @JsonProperty("docVersion")
    private Integer docVersion;

    /** Only present on livePhoto-style entries (e.g. memberPhoto.inputData.livePhotoScore). Kept for completeness. */
    @JsonProperty("livePhotoScore")
    private String livePhotoScore;

    @JsonProperty("mappingId")
    private String mappingId;
}
