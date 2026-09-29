package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.AuditTrailService;

@Component
public class PersonalDetailsUpdateHandler implements UpdateHandler {

    private final CmCustomerRepository customerRepo;
    private final AuditTrailService auditService;

    public PersonalDetailsUpdateHandler(CmCustomerRepository customerRepo, AuditTrailService auditService) {
        this.customerRepo = customerRepo;
        this.auditService = auditService;
    }

    @Override
    public String getSectionName() {
        return "PERSONAL_DETAILS";
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

        if (payload.containsKey("customerName")) cust.setCustomerName((String) payload.get("customerName"));
        if (payload.containsKey("dob")) cust.setDob((String) payload.get("dob"));
        if (payload.containsKey("maritalStatus")) cust.setMaritalStatus((String) payload.get("maritalStatus"));

        cust.setUpdatedBy(header != null ? header.getUserId() : "SYSTEM");
        cust.setUpdatedTs(LocalDateTime.now());
        customerRepo.save(cust);

        // Audit Trail
        auditService.logAudit(cust.getApplicationId(), cust.getCustomerId(),
                header != null ? header.getUserId() : "SYSTEM",
                header != null ? header.getUserId() : "SYSTEM",
                header != null ? header.getUserRole() : "KM",
                "PERSONAL_DETAILS", "DRAFT", "IN_PROGRESS", payload, payload);

        return UpdateResponseDto.builder()
                .applicationId(cust.getApplicationId())
                .customerId(cust.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus("PENDING_VERIFICATION")
                .build();
    }
}
