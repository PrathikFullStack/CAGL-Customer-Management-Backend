package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustOthers;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import com.iexceed.appzillonbanking.cagl.cob.payload.IncomeDet;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustOthersRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustomerRepository;
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
public class IncomeHandler implements SubStageHandler {
    private final DocumentService documentService;
    private final TbObCustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
        if (cd.getPayload() != null) {
            payload.putAll(cd.getPayload());
        }

        TbObCustomer updatedCustomer = customerRepository.findByApplicationId(applicationId)
                .orElseGet(() -> TbObCustomer.builder()
                        .applicationId(applicationId)
                        .customerId(customer.getCustomerId())
                        .createdTs(now)
                        .build());
        updatedCustomer.setCustomerId(customer.getCustomerId());

        IncomeDet inc = cd.getIncomeDet();
        if (inc != null) {
            updatedCustomer.setIncomedet(objectMapper.writeValueAsString(inc.incomePayload()));
            updatedCustomer.setQuestionnaire(objectMapper.writeValueAsString(inc.questPayload()));
        }
        customer.setPayload(payload);
        customerRepository.save(updatedCustomer);
        if (inc != null && !CollectionUtils.isEmpty(inc.documentList())) {
            for (DocumentListItem document : inc.documentList()) {
                documentService.saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), userId, now);
            }
        }
    }
}
