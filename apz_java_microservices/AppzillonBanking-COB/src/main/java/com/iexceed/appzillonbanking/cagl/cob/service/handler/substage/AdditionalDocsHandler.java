package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.AdditionalDocuDet;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import com.iexceed.appzillonbanking.cagl.cob.service.DocumentService;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdditionalDocsHandler implements SubStageHandler {
    private final DocumentService documentService;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        AdditionalDocuDet ad = cd.getAdditionalDocuDet();

        if (ad == null || CollectionUtils.isEmpty(ad.getDocumentList())) {
            return;
        }
        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }
        for (DocumentListItem document : ad.getDocumentList()) {
            documentService.saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), userId, now);
        }
    }
}
