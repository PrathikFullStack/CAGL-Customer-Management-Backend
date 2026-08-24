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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Substage 1.8 - primary member's photo / liveness capture, plus any documentList photo entries. */
@Component
public class PhotoUpdateHandler extends AbstractDocumentListHandler implements UpdateHandler {

    private static final List<String> PAYLOAD_FIELDS = List.of(
            "customerPhotoDocId", "livenessScore", "clarityScore",
            "gpsLatitude", "gpsLongitude", "gpsAccuracy");

    public PhotoUpdateHandler(TbObDocumentRepository documentRepository,
                              TbObApplnWorkflowRepository workflowVersionRepository,
                              ObjectMapper objectMapper) {
        super(documentRepository, workflowVersionRepository, objectMapper);
    }

    @Override
    public UpdateType supports() {
        return UpdateType.PHOTO;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        List<String> changed = new ArrayList<>();

        Map<String, Object> payload = new HashMap<>(ctx.customer().getPayload() != null
                ? ctx.customer().getPayload() : Map.of());
        for (String field : PAYLOAD_FIELDS) {
            String value = JsonNodeUtil.text(p, field);
            if (value != null && JsonNodeUtil.differs(payload.get(field), value)) {
                payload.put(field, value);
                changed.add(field);
            }
        }
        ctx.customer().setPayload(payload);

        changed.addAll(upsertDocuments(ctx, JsonNodeUtil.array(p, "documentList")));
        return HandlerResult.of(changed, "1.8");
    }
}
