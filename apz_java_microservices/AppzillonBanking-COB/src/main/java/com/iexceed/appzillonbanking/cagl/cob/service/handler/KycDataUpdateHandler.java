package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/** Substage 1.2 - primary KYC documents (Voter ID / Aadhaar / PAN, docu_id 1-3). */
@Component
public class KycDataUpdateHandler extends AbstractDocumentListHandler implements UpdateHandler {

    public KycDataUpdateHandler(TbObDocumentRepository documentRepository,
                                TbObApplnWorkflowRepository workflowVersionRepository,
                                ObjectMapper objectMapper) {
        super(documentRepository, workflowVersionRepository, objectMapper);
    }

    @Override
    public UpdateType supports() {
        return UpdateType.KYC_DATA;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode documentList = JsonNodeUtil.array(ctx.payload(), "documentList");
        List<String> changed = upsertDocuments(ctx, documentList);

        // Primary KYC id/type on the customer master follows docu_id=1 (the primary document).
        JsonNode primary = firstDocByIdType(documentList, "Primary");
        if (primary != null) {
            String legalDocName = JsonNodeUtil.text(primary, "legalDocName");
            String legalDocId = JsonNodeUtil.text(primary, "legaldocId", JsonNodeUtil.text(primary, "legalDocId"));
            if (legalDocName != null && JsonNodeUtil.differs(ctx.customer().getPrimaryKycType(), legalDocName)) {
                ctx.customer().setPrimaryKycType(legalDocName);
                changed.add("primaryKycType");
            }
            if (legalDocId != null && JsonNodeUtil.differs(ctx.customer().getPrimaryKycId(), legalDocId)) {
                ctx.customer().setPrimaryKycId(legalDocId);
                changed.add("primaryKycId");
            }
        }

        String ckycId = JsonNodeUtil.text(ctx.payload(), "ckycId");
        if (ckycId != null) {
            var kyc = new java.util.HashMap<>(ctx.customer().getKycDetails() != null
                    ? ctx.customer().getKycDetails() : java.util.Map.of());
            if (JsonNodeUtil.differs(kyc.get("ckycId"), ckycId)) {
                kyc.put("ckycId", ckycId);
                ctx.customer().setKycDetails(kyc);
                changed.add("kycDetails.ckycId");
            }
        }

        return HandlerResult.of(changed, "1.2");
    }

    private JsonNode firstDocByIdType(JsonNode documentList, String idType) {
        if (documentList == null) {
            return null;
        }
        for (JsonNode entry : documentList) {
            JsonNode details = entry.has("documentDetails") ? entry.get("documentDetails") : entry;
            if (idType.equalsIgnoreCase(JsonNodeUtil.text(details, "IDtype"))) {
                return details;
            }
        }
        return null;
    }
}
