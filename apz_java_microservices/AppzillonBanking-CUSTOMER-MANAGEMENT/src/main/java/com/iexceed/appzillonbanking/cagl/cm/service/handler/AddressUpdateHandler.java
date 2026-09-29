package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmAddressEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmAddressRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.AuditTrailService;

@Component
public class AddressUpdateHandler implements UpdateHandler {

    private final CmAddressRepository addressRepo;
    private final AuditTrailService auditService;
    private final ObjectMapper objectMapper;

    public AddressUpdateHandler(CmAddressRepository addressRepo, AuditTrailService auditService, ObjectMapper objectMapper) {
        this.addressRepo = addressRepo;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSectionName() {
        return "ADDRESS";
    }

    @Override
    @Transactional("primaryTransactionManager")
    public UpdateResponseDto handleUpdate(CustomerUpdateRequest request, RequestHeader header) {
        Map<String, Object> payload = request.getUpdatePayload();
        String addressType = (String) payload.getOrDefault("addressType", "C");

        Optional<CmAddressEntity> addrOpt = addressRepo.findByCustomerIdAndAddressType(request.getCustomerId(), addressType);
        if (addrOpt.isPresent()) {
            CmAddressEntity addr = addrOpt.get();
            try {
                addr.setAddrPayload(objectMapper.writeValueAsString(payload.get("addressDetails")));
                if (payload.containsKey("distanceFromBranch")) {
                    addr.setDistanceFromBranch(payload.get("distanceFromBranch").toString());
                }
                if (payload.containsKey("addressProofType")) {
                    addr.setAddressProofType((String) payload.get("addressProofType"));
                }
                if (payload.containsKey("addressProofDocId")) {
                    addr.setAddressProofDocId((String) payload.get("addressProofDocId"));
                }
                addr.setUpdatedBy(header != null ? header.getUserId() : "SYSTEM");
                addr.setUpdatedTs(LocalDateTime.now());
                addressRepo.save(addr);

                auditService.logAudit(addr.getApplicationId(), request.getCustomerId(),
                        header != null ? header.getUserId() : "SYSTEM",
                        header != null ? header.getUserId() : "SYSTEM",
                        header != null ? header.getUserRole() : "KM",
                        "ADDRESS", "DRAFT", "IN_PROGRESS", payload, payload);

            } catch (Exception ex) {
                return UpdateResponseDto.builder()
                        .customerId(request.getCustomerId())
                        .section(getSectionName())
                        .status("FAILED")
                        .build();
            }
        }

        return UpdateResponseDto.builder()
                .customerId(request.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus("PENDING_VERIFICATION")
                .build();
    }
}
