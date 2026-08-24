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

/** Substage 1.9 - house photo / business photo / other supporting documents. */
@Component
public class DocumentsUpdateHandler extends AbstractDocumentListHandler implements UpdateHandler {

    public DocumentsUpdateHandler(TbObDocumentRepository documentRepository,
                                  TbObApplnWorkflowRepository workflowRepository,
                                  ObjectMapper objectMapper) {
        super(documentRepository, workflowRepository, objectMapper);
    }

    @Override
    public UpdateType supports() {
        return UpdateType.DOCUMENTS;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode documentList = JsonNodeUtil.array(ctx.payload(), "documentList");
        List<String> changed = upsertDocuments(ctx, documentList);
        return HandlerResult.of(changed, "1.9");
    }
}
