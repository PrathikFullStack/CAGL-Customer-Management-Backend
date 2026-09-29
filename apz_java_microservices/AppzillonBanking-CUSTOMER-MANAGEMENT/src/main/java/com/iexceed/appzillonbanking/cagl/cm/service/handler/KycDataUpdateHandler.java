package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.AuditTrailService;

@Component
public class KycDataUpdateHandler implements UpdateHandler {

    private final CmCustomerRepository customerRepo;
    private final AuditTrailService auditService;
    private final ObjectMapper objectMapper;

    public KycDataUpdateHandler(
            CmCustomerRepository customerRepo,
            AuditTrailService auditService,
            ObjectMapper objectMapper) {
        this.customerRepo = customerRepo;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSectionName() {
        return "KYC_DETAILS";
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

        if (payload.containsKey("primaryKycType")) cust.setPrimaryKycType((String) payload.get("primaryKycType"));
        if (payload.containsKey("primaryKycId")) cust.setPrimaryKycId((String) payload.get("primaryKycId"));

        try {
            cust.setKycDetails(objectMapper.writeValueAsString(payload));
            cust.setKycStatus("PENDING_VERIFICATION");
            cust.setUpdatedBy(header != null ? header.getUserId() : "SYSTEM");
            cust.setUpdatedTs(LocalDateTime.now());
            customerRepo.save(cust);

            auditService.logAudit(cust.getApplicationId(), cust.getCustomerId(),
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserRole() : "KM",
                    "KYC_DETAILS", "DRAFT", "PENDING_RPC_MAKER", payload, payload);

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
                .workflowStatus("PENDING_RPC_MAKER")
                .nextRole("RPC_MAKER")
                .build();
    }
}
