package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.BankDet;
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
public class BankHandler implements SubStageHandler {
    private final DocumentService documentService;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        BankDet bd = cd.getBankDet();
        if (bd == null) {
            return;
        }

        Map<String, Object> bankMap = new HashMap<>();
        bankMap.put("bankAccNo", bd.getBankAccNo());
        bankMap.put("bankAccName", bd.getBankAccName());
        bankMap.put("bankBranchName", bd.getBankBranchName());
        bankMap.put("bankName", bd.getBankName());
        bankMap.put("bankIfscCode", bd.getBankIfscCode());
        bankMap.put("status", bd.getStatus());
        bankMap.put("pennyRes", bd.getPennyRes());
        customer.setBankDetails(bankMap);

        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }

        if (!CollectionUtils.isEmpty(bd.getDocumentList())) {
            for (DocumentListItem document : bd.getDocumentList()) {
                documentService.saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), userId, now);
            }
        }
    }
}
