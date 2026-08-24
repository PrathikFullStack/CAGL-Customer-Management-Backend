package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocument;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocumentId;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared "documentList[]" upsert logic reused by every substage that
 * carries documents (KYC_DATA, ADDRESS proofs, FAMILY_MEMBERS, PHOTO,
 * DOCUMENTS). Each entry is keyed by (applicationId, docuId) - a composite,
 * application-scoped running index per the schema remark on tb_ob_document.
 * <p>
 * When the DMS reference for an existing docu_id changes (re-upload), a new
 * tb_ob__appln_workflow row is appended and the previous version superseded,
 * per the doc_version / is_latest versioning contract.
 */
abstract class AbstractDocumentListHandler {

    protected final TbObDocumentRepository documentRepository;
    protected final TbObApplnWorkflowRepository workflowRepository;
    protected final ObjectMapper objectMapper;

    protected AbstractDocumentListHandler(TbObDocumentRepository documentRepository,
                                          TbObApplnWorkflowRepository workflowRepository,
                                           ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.workflowRepository = workflowRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * @return names of fields that changed, prefixed with docu_id, e.g. "doc[2].legalDocId"
     */
    protected List<String> upsertDocuments(UpdateContext ctx, JsonNode documentListNode) {
        List<String> changed = new ArrayList<>();
        if (documentListNode == null) {
            return changed;
        }

        for (JsonNode entry : documentListNode) {
            JsonNode details = entry.has("documentDetails") ? entry.get("documentDetails") : entry;
            String docuId = JsonNodeUtil.text(details, "docuId");
            if (docuId == null) {
                continue;
            }
            changed.addAll(upsertOne(ctx, docuId, details));
        }
        return changed;
    }

    private List<String> upsertOne(UpdateContext ctx, String docuId, JsonNode details) {
        List<String> changed = new ArrayList<>();
        String applicationId = ctx.applicationId();

        TbObDocument doc = documentRepository
                .findById(new TbObDocumentId(applicationId, docuId))
                .orElseGet(() -> TbObDocument.builder()
                        .applicationId(applicationId)
                        .docuId(docuId)
                        .customerId(ctx.customerId())
                        .isEdited(false)
                        .docVersion(1)
                        .createdTs(ctx.nowEpochMillis())
                        .build());

        boolean isNew = doc.getUploadedAt() == null;

        String category = JsonNodeUtil.text(details, "category", JsonNodeUtil.text(details, "catogory"));
        String subCat = JsonNodeUtil.text(details, "SubCat", JsonNodeUtil.text(details, "subCat"));
        String kycType = JsonNodeUtil.text(details, "kycType");
        String legalDocName = JsonNodeUtil.text(details, "legalDocName");
        String legalDocId = JsonNodeUtil.text(details, "legaldocId", JsonNodeUtil.text(details, "legalDocId"));
        String docuNoF = JsonNodeUtil.text(details, "docuNoF");
        String docuNoB = JsonNodeUtil.text(details, "docuNoB");
        String photo = JsonNodeUtil.text(details, "photo");

        applyIfChanged(doc.getCategory(), category, doc::setCategory, "category", docuId, changed);
        applyIfChanged(doc.getSubCat(), subCat, doc::setSubCat, "subCat", docuId, changed);
        applyIfChanged(doc.getKycType(), kycType, doc::setKycType, "kycType", docuId, changed);
        applyIfChanged(doc.getLegalDocName(), legalDocName, doc::setLegalDocName, "legalDocName", docuId, changed);
        applyIfChanged(doc.getLegalDocId(), legalDocId, doc::setLegalDocId, "legalDocId", docuId, changed);

        boolean dmsChanged = JsonNodeUtil.differs(doc.getDmsDocIdFront(), docuNoF)
                || JsonNodeUtil.differs(doc.getDmsDocIdBack(), docuNoB)
                || JsonNodeUtil.differs(doc.getPhoto(), photo);

        if (docuNoF != null) {
            applyIfChanged(doc.getDmsDocIdFront(), docuNoF, doc::setDmsDocIdFront, "docuNoF", docuId, changed);
        }
        if (docuNoB != null) {
            applyIfChanged(doc.getDmsDocIdBack(), docuNoB, doc::setDmsDocIdBack, "docuNoB", docuId, changed);
        }
        if (photo != null) {
            applyIfChanged(doc.getPhoto(), photo, doc::setPhoto, "photo", docuId, changed);
        }

        JsonNode ocrData = details.get("oCRData");
        JsonNode inputData = details.get("inputData");
        if (ocrData != null && !ocrData.isNull()) {
//            doc.setOcrData(JsonNodeUtil.asMap(objectMapper, ocrData));
        }
        if (inputData != null && !inputData.isNull()) {
//            doc.setInputData(JsonNodeUtil.asMap(objectMapper, inputData));
        }

        Boolean isEdited = JsonNodeUtil.bool(details, "isEdited");
        if (Boolean.TRUE.equals(isEdited)) {
            doc.setIsEdited(true);
            doc.setEditedBy(JsonNodeUtil.text(details, "editedBy", ctx.actorUserId()));
        }
        String reuploadedBy = JsonNodeUtil.text(details, "reUploadedBy");
        if (reuploadedBy != null) {
            doc.setReuploadedBy(reuploadedBy);
        }

        doc.setUploadedBy(JsonNodeUtil.text(details, "editedBy", ctx.actorUserId()));
        doc.setUploadedAt(ctx.nowEpochMillis());
        doc.setUpdatedTs(ctx.nowEpochMillis());

        if (!isNew && dmsChanged) {
            supersedeAndVersion(ctx);
            doc.setDocVersion(doc.getDocVersion() + 1);
        } else if (isNew && (docuNoF != null || photo != null)) {
            recordInitialVersion(ctx);
        }

        documentRepository.save(doc);
        return changed;
    }

    private void supersedeAndVersion(UpdateContext ctx) {
//        workflowRepository.supersedeCurrentVersion(ctx.applicationId());
        Integer nextVersionNo = workflowRepository
                .findByApplicationId(ctx.applicationId())
                .map(v -> v.getVersionNo() + 1)
                .orElse(2);
        saveVersion(ctx, nextVersionNo, "RPC_CORRECTION");
    }

    private void recordInitialVersion(UpdateContext ctx) {
        saveVersion(ctx, 1, "ORIGINAL");
    }

    private void saveVersion(UpdateContext ctx, Integer versionNo, String reason) {
        workflowRepository.save(TbObApplnWorkflow.builder()
                .applicationId(ctx.applicationId())
                .versionNo(versionNo)
                .createdBy(ctx.actorUserId())
                .createdTs(ctx.nowEpochMillis())
                .build());
    }

    private void applyIfChanged(String oldValue, String newValue, java.util.function.Consumer<String> setter,
                                 String fieldName, String docuId, List<String> changed) {
        if (newValue != null && JsonNodeUtil.differs(oldValue, newValue)) {
            setter.accept(newValue);
            changed.add("doc[" + docuId + "]." + fieldName);
        }
    }
}
