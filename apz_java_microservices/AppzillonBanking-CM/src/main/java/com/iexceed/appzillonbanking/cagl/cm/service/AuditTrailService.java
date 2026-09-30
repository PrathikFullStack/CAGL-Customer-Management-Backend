package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustAuditTrailEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustAuditTrailRepository;

@Service
public class AuditTrailService {

    private static final Logger logger = LogManager.getLogger(AuditTrailService.class);

    private final CmCustAuditTrailRepository auditRepo;
    private final ObjectMapper objectMapper;

    public AuditTrailService(CmCustAuditTrailRepository auditRepo, ObjectMapper objectMapper) {
        this.auditRepo = auditRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Records a field-level change audit log in tb_cm_cust_audit_trail
     */
    @Transactional("primaryTransactionManager")
    public void logAudit(String applicationId, String customerId, String userId, String userName,
                         String userRole, String stageId, String subStage, String wfStatus,
                         Object editedDetails, Object fullPayload) {
        try {
            String editedJson = editedDetails != null ? objectMapper.writeValueAsString(editedDetails) : null;
            String payloadJson = fullPayload != null ? objectMapper.writeValueAsString(fullPayload) : null;

            CmCustAuditTrailEntity audit = CmCustAuditTrailEntity.builder()
                    .id("AUD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .applicationId(applicationId)
                    .customerId(customerId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .stageId(stageId)
                    .subStage(subStage)
                    .wfstatus(wfStatus)
                    .isedited(editedDetails != null ? "Y" : "N")
                    .editeddetails(editedJson)
                    .payload(payloadJson)
                    .createTs(LocalDateTime.now())
                    .build();

            auditRepo.save(audit);
            logger.debug("Logged audit record for Customer: {}, Stage: {}", customerId, stageId);
        } catch (Exception ex) {
            logger.error("Failed to write audit trail: {}", ex.getMessage());
        }
    }
}
