package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.client.KycServiceClient;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.AuditTrailService;

@Component
public class BankDetailsUpdateHandler implements UpdateHandler {

    private final CmCustomerRepository customerRepo;
    private final KycServiceClient kycClient;
    private final AuditTrailService auditService;
    private final ObjectMapper objectMapper;

    public BankDetailsUpdateHandler(
            CmCustomerRepository customerRepo,
            KycServiceClient kycClient,
            AuditTrailService auditService,
            ObjectMapper objectMapper) {
        this.customerRepo = customerRepo;
        this.kycClient = kycClient;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSectionName() {
        return "BANK_DETAILS";
    }

    @Override
    @Transactional("primaryTransactionManager")
    public UpdateResponseDto handleUpdate(CustomerUpdateRequest request, RequestHeader header) {
        Optional<CmCustomerEntity> custOpt = customerRepo.findByCustomerId(request.getCustomerId());
        if (custOpt.isEmpty()) {
            return UpdateResponseDto.builder()
                    .customerId(request.getCustomerId())
                    .section(getSectionName())
                    .status("FAILED")
                    .build();
        }

        CmCustomerEntity cust = custOpt.get();
        Map<String, Object> payload = request.getUpdatePayload();

        String acNo = (String) payload.get("accountNumber");
        String ifsc = (String) payload.get("ifscCode");

        // 1. Run Penny Drop Validation
        Map<String, Object> pennyDropRes = kycClient.verifyPennyDrop(acNo, ifsc, cust.getCustomerName());

        try {
            payload.put("verificationStatus", pennyDropRes.get("status"));
            payload.put("verifiedDate", LocalDateTime.now().toString());

            cust.setBankDetails(objectMapper.writeValueAsString(payload));
            cust.setUpdatedBy(header != null ? header.getUserId() : "SYSTEM");
            cust.setUpdatedTs(LocalDateTime.now());
            customerRepo.save(cust);

            // 2. Audit Trail
            auditService.logAudit(cust.getApplicationId(), cust.getCustomerId(),
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserRole() : "KM",
                    "BANK_DETAILS", "DRAFT", "PENDING_CHECKER", payload, payload);

        } catch (Exception ex) {
            return UpdateResponseDto.builder()
                    .customerId(request.getCustomerId())
                    .section(getSectionName())
                    .status("FAILED")
                    .build();
        }

        return UpdateResponseDto.builder()
                .applicationId(cust.getApplicationId())
                .customerId(cust.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus("PENDING_RPC_CHECKER")
                .nextRole("RPC_CHECKER")
                .verificationDetails(pennyDropRes)
                .build();
    }
}
