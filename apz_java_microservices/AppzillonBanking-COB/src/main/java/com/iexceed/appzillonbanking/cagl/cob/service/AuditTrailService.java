package com.iexceed.appzillonbanking.cagl.cob.service;

import com.iexceed.appzillonbanking.cagl.cob.enums.AuditEventType;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final TbObCustAuditTrailRepository custAuditTrailRepository;

    /**
     * APPLICATION_VIEWED event mandated by API 9; other APIs (create/update)
     * will call this with their own event codes.
     */
    public void recordEvent(TbObApplicationMaster app, AuditEventType eventType,
                            String userId, String userName, String userRole) {

        LocalDateTime now = LocalDateTime.now();

        TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                .appId(app.getApplicationId())
                .applicationId(app.getApplicationId())
                .customerId(String.valueOf(app.getCustomerId()))
                .kendraId(String.valueOf(app.getKendraId()))
                .groupId(String.valueOf(app.getGroupId()))
                .userId(userId)
                .userName(userName)
                .userRole(userRole)
                .stageId(app.getStage())
                .subStage(app.getSubStage())
                .wfStatus(app.getWfStage())
                .customerName(app.getCustomerName())
                .kendraName(app.getKendraName())
                .branchId(app.getBranchId())
                .payload(Map.of(
                        "event", eventType.name(),
                        "applicationId", app.getApplicationId(),
                        "status", app.getStatus() == null ? "" : app.getStatus()
                ))
                .createTs(now)
                .build();

        custAuditTrailRepository.save(audit);
    }

    public void recordFieldUpdated(TbObApplicationMaster app, String actorUserId, String actorRole,
                                   List<String> changedFields, LocalDateTime now) {
        if (changedFields.isEmpty()) {
            return;
        }
        custAuditTrailRepository.save(baseRow(app, actorUserId, actorRole, now)
                .addInfo1(Map.of("event", "FIELD_UPDATED", "changedFields", changedFields))
                .build());
    }

    public void recordSubstageTransition(TbObApplicationMaster app, String actorUserId, String actorRole,
                                         String fromSubStage, String toSubStage, LocalDateTime now) {
        if (toSubStage == null || toSubStage.equals(fromSubStage)) {
            return;
        }
        custAuditTrailRepository.save(baseRow(app, actorUserId, actorRole, now)
                .addInfo1(Map.of("event", "SUBSTAGE_TRANSITION", "from", String.valueOf(fromSubStage), "to", toSubStage))
                .build());
    }

    private TbObCustAuditTrail.TbObCustAuditTrailBuilder baseRow(TbObApplicationMaster app, String actorUserId,
                                                         String actorRole, LocalDateTime now) {
        return TbObCustAuditTrail.builder()
                .applicationId(app.getApplicationId())
                .userId(actorUserId)
                .userRole(actorRole)
                .stageId(app.getStage())
                .subStage(app.getSubStage())
                .wfStatus(app.getStatus())
                .customerId(String.valueOf(app.getCustomerId()))
                .customerName(app.getCustomerName())
                .kendraId(String.valueOf(app.getKendraId()))
                .kendraName(app.getKendraName())
                .groupId(String.valueOf(app.getGroupId()))
                .branchId(app.getBranchId())
                .createTs(now);
    }
}
