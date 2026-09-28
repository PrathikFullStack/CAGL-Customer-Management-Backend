package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocument;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocumentId;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final TbObDocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    /** Overload for document types whose category comes straight from the payload (default). */
    public void saveDocument(DocumentDetail doc, String applicationId, String customerId, String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        saveDocument(doc, applicationId, customerId, uploadedBy, now, doc.getCategory());
    }

    /**
     * Maps one wire-level {@link DocumentDetail} onto a {@code tb_ob_document} row and persists it.
     * docu_id is issued by the client and is stable/unique per application, so this is an upsert
     * (save acts as insert-or-update against the composite PK).
     * <p>
     * NOTE: legal_doc_name and kyc_type are NOT NULL on tb_ob_document, but a few document types
     * (e.g. the live member photo, plain home/business photos) don't carry either field in the
     * payload. Defensive defaults are applied below; confirm with the product/schema owner whether
     * these columns should be made nullable for non-KYC document categories instead.
     */
    public void saveDocument(DocumentDetail doc, String applicationId, String customerId, String uploadedBy,
                              LocalDateTime now, String categoryOverride) throws JsonProcessingException {
        TbObDocument document = documentRepository.findById(
                        new TbObDocumentId(applicationId, doc.getDocuId()))
                .orElse(TbObDocument.builder()
                        .applicationId(applicationId)
                        .docuId(doc.getDocuId())
                        .createdTs(now)
                        .docVersion(1)
                        .build());

        boolean isUpdate = document.getUploadedAt() != null;

        document.setDocuId(doc.getDocuId());
        document.setCustomerId(customerId);
        document.setCategory(categoryOverride != null ? categoryOverride : "OTHER");
        document.setSubCat(doc.getSubCat() != null ? doc.getSubCat() : "NA");
        document.setKycType(doc.getKycType() != null ? doc.getKycType() : "NON-KYC");
        document.setAuthMode(doc.getAuthMode());
        document.setIdType(doc.getIdType());
        document.setMemRelation(doc.getMemRelation());
        document.setMappingId(doc.getMappingId() != null ? doc.getMappingId() : null);
        document.setLegalDocName(doc.getLegalDocName() != null ? doc.getLegalDocName() : document.getSubCat());
        document.setLegalDocId(doc.getLegalDocId());
        document.setDmsDocIdFront(doc.getDocuNoF());
        document.setDmsDocIdBack(doc.getDocuNoB());
        document.setPhoto(doc.getPhoto());
        document.setPayload(objectMapper.writeValueAsString(doc.getPayload()));
        document.setStatus(doc.getStatus() != null ? doc.getStatus() : "uncaptured");
        document.setScore(doc.getLivePhotoScore());
        document.setIsEdited(Boolean.TRUE.equals(doc.getIsEdited()));
        document.setEditedBy(doc.getEditedBy());
        document.setEditedFields(doc.getEditedFields());
        document.setReuploadedBy(doc.getReUploadedBy());
        document.setReason(doc.getReason());
        document.setClarityScore(doc.getClarityScore());
        document.setClarityPass(doc.getClarityPass());
        document.setDedupeStatus(doc.getDedupeStatus());
        document.setValidationStatus("pending");
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(now);
        document.setUpdatedTs(now);
        if (isUpdate) {
            document.setDocVersion(document.getDocVersion() + 1);
        }

        documentRepository.save(document);
    }
}
