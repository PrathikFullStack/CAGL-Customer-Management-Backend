package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmFamilyMemberEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmFamilyMemberRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.AuditTrailService;

@Component
public class FamilyMemberUpdateHandler implements UpdateHandler {

    private final CmFamilyMemberRepository familyRepo;
    private final AuditTrailService auditService;
    private final ObjectMapper objectMapper;

    public FamilyMemberUpdateHandler(
            CmFamilyMemberRepository familyRepo,
            AuditTrailService auditService,
            ObjectMapper objectMapper) {
        this.familyRepo = familyRepo;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSectionName() {
        return "FAMILY_DETAILS";
    }

    @Override
    @Transactional("primaryTransactionManager")
    public UpdateResponseDto handleUpdate(CustomerUpdateRequest request, RequestHeader header) {
        Map<String, Object> payload = request.getUpdatePayload();
        String memberType = (String) payload.getOrDefault("memberType", "CO");

        try {
            CmFamilyMemberEntity member = CmFamilyMemberEntity.builder()
                    .familyMemId("FAM_" + UUID.randomUUID().toString().substring(0, 6))
                    .customerId(request.getCustomerId())
                    .applicationId(request.getApplicationId())
                    .memberType(memberType)
                    .relation((String) payload.get("relation"))
                    .name((String) payload.get("name"))
                    .kycType((String) payload.get("kycType"))
                    .kycDocId((String) payload.get("kycDocId"))
                    .isNominee(payload.containsKey("isNominee") ? (Boolean) payload.get("isNominee") : false)
                    .isEarningMember(payload.containsKey("isEarningMember") ? (Boolean) payload.get("isEarningMember") : false)
                    .nomineeBankDetails(payload.containsKey("bankDetails") ? objectMapper.writeValueAsString(payload.get("bankDetails")) : null)
                    .createdTs(LocalDateTime.now())
                    .build();

            familyRepo.save(member);

            auditService.logAudit(request.getApplicationId(), request.getCustomerId(),
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserId() : "SYSTEM",
                    header != null ? header.getUserRole() : "KM",
                    "FAMILY_DETAILS", "DRAFT", "PENDING_VERIFICATION", payload, payload);

        } catch (Exception ex) {
            return UpdateResponseDto.builder()
                    .customerId(request.getCustomerId())
                    .section(getSectionName())
                    .status("FAILED")
                    .build();
        }

        return UpdateResponseDto.builder()
                .applicationId(request.getApplicationId())
                .customerId(request.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus("PENDING_VERIFICATION")
                .build();
    }
}
