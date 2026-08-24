package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.constants.AuditConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObUserAuditTrail;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailRecord;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.GlobalSearchRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObUserAuditTrailRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.CustAuditTrailRepository;
import com.iexceed.appzillonbanking.core.payload.Header;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final TbObUserAuditTrailRepository userAuditTrailRepository;
    private final CustAuditTrailRepository custAuditTrailRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generic User Audit
     */
    public void saveUserAudit(String eventType,
                              String applicationId,
                              String customerId,
                              String userId,
                              String userName,
                              String userRole,
                              String branchId,
                              String kendraId,
                              String groupId,
                              Object request) {

        try {

            TbObUserAuditTrail audit = TbObUserAuditTrail.builder()
                    .applicationId(applicationId)
                    .customerId(customerId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .branchId(branchId)
                    .kendraId(kendraId)
                    .groupId(groupId)
                    .serviceType(eventType)
                    .payload(convertPayload(request))
                    .build();

            userAuditTrailRepository.save(audit);

            log.info("User Audit Saved Successfully :: {}", eventType);

        } catch (Exception e) {

            log.error("Error while saving User Audit", e);

        }
    }

    /**
     * Generic Application Audit
     */
    public void saveApplicationAudit(String applicationId,
                                     String customerId,
                                     String userId,
                                     String userName,
                                     String userRole,
                                     String stageId,
                                     String subStage,
                                     String wfStatus,
                                     String productId,
                                     String loanAmount,
                                     String purpose,
                                     String repaymentFrequency,
                                     String mobileNo,
                                     String customerName,
                                     String branchId,
                                     Object request) {

        try {

            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .applicationId(applicationId)
                    .customerId(customerId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .stageId(stageId)
                    .subStage(subStage)
                    .wfStatus(wfStatus)
                    .productId(productId)
                    .loanAmt(loanAmount)
                    .purpose(purpose)
                    .repaymentFrequency(repaymentFrequency)
                    .mobileNo(mobileNo)
                    .customerName(customerName)
                    .branchId(branchId)
                    .payload(convertPayload(request))
                    .build();

            custAuditTrailRepository.save(audit);

            log.info("Application Audit Saved Successfully :: ApplicationId={}", applicationId);

        } catch (Exception e) {

            log.error("Error while saving Application Audit", e);

        }
    }

    /**
     * Dashboard View Audit
     */
    public void logDashboardViewEvent(DashboardListRequestFields fields) {

        if (fields == null) {
            return;
        }

        saveUserAudit(
                AuditConstants.DASHBOARD_VIEWED,
                null, // applicationId is not relevant for a dashboard view
                null,
                fields.getUserId(),
                null,
                fields.getUserRole(),
                fields.getBranchId(),
                null,
                null,
                fields
        );
    }

    /**
     * Global Search Audit
     */
    public void logGlobalSearchEvent(GlobalSearchRequestFields fields, Header header) {

        if (fields == null) {
            return;
        }

        saveUserAudit(
                AuditConstants.GLOBAL_SEARCH,
                null, // applicationId is not relevant for a global search
                null,
                fields.getUserId(),
                null,
                fields.getUserRole(),
                null,
                null,
                null,
                fields
        );
    }

    /**
     * Audit Trail Viewed
     */
    public void logAuditTrailViewed(String applicationId,
                                    String customerId,
                                    String userId,
                                    String userRole) {

        saveUserAudit(
                AuditConstants.AUDIT_TRAIL_VIEWED,
                applicationId,
                customerId,
                userId,
                null,
                userRole,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Convert Object to JSON Payload
     */
    private Map<String, Object> convertPayload(Object object) {

        if (object == null) {
            return new HashMap<>();
        }

        return objectMapper.convertValue(
                object,
                new TypeReference<Map<String, Object>>() {
                });
    }
    /**
     * Fetch Combined User + Application Audit Trail
     */
    public List<AuditTrailRecord> fetchCombinedAuditTrail(String applicationId,
                                                          String customerId,
                                                          String userId,
                                                          String userRole) {

        if (!StringUtils.hasText(applicationId)
                && !StringUtils.hasText(customerId)) {
            return new ArrayList<>();
        }

        // Audit Trail Viewed Event
        logAuditTrailViewed(
                applicationId,
                customerId,
                userId,
                userRole);

        List<TbObUserAuditTrail> userAudits;
        List<TbObCustAuditTrail> applicationAudits;

        if (StringUtils.hasText(applicationId)) {

            userAudits =
                    userAuditTrailRepository.findByApplicationId(applicationId);

            applicationAudits =
                    custAuditTrailRepository.findByApplicationId(applicationId);

        } else {

            userAudits =
                    userAuditTrailRepository.findByCustomerId(customerId);

            applicationAudits =
                    custAuditTrailRepository.findByCustomerId(customerId);
        }

        Stream<AuditTrailRecord> userStream =
                userAudits.stream().map(this::convertUserAudit);

        Stream<AuditTrailRecord> applicationStream =
                applicationAudits.stream().map(this::convertApplicationAudit);

        return Stream.concat(userStream, applicationStream)
                .sorted(Comparator.comparing(AuditTrailRecord::getEventTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Convert User Audit Entity to Response
     */
    private AuditTrailRecord convertUserAudit(TbObUserAuditTrail audit) {

        String details = audit.getServiceType();

        if (StringUtils.hasText(audit.getSubStage())) {
            details += " - " + audit.getSubStage();
        }

        return AuditTrailRecord.builder()
                .auditSource("USER")
                .eventType(audit.getServiceType())
                .eventTimestamp(audit.getCreateTs())
                .userId(audit.getUserId())
                .userName(audit.getUserName())
                .userRole(audit.getUserRole())
                .details(details)
                .build();
    }

    /**
     * Convert Application Audit Entity to Response
     */
    private AuditTrailRecord convertApplicationAudit(TbObCustAuditTrail audit) {

        StringBuilder details = new StringBuilder();

        if (StringUtils.hasText(audit.getStageId())) {
            details.append("Stage : ")
                    .append(audit.getStageId());
        }

        if (StringUtils.hasText(audit.getSubStage())) {
            details.append(" | SubStage : ")
                    .append(audit.getSubStage());
        }

        if (StringUtils.hasText(audit.getWfStatus())) {
            details.append(" | Status : ")
                    .append(audit.getWfStatus());
        }

        return AuditTrailRecord.builder()
                .auditSource("APPLICATION")
                .eventType(audit.getStageId())
                .eventTimestamp(audit.getCreateTs())
                .userId(audit.getUserId())
                .userName(audit.getUserName())
                .userRole(audit.getUserRole())
                .details(details.toString())
                .build();
    }
    /**
     * Generic User Audit with Custom Payload
     */
    public void saveUserAudit(String applicationId,
                              String customerId,
                              String userId,
                              String userName,
                              String userRole,
                              String eventType,
                              String subStage,
                              Object payload,
                              Object addInfo1,
                              Object addInfo2) {

        try {

            TbObUserAuditTrail audit = TbObUserAuditTrail.builder()
                    .applicationId(applicationId)
                    .customerId(customerId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .serviceType(eventType)
                    .subStage(subStage)
                    .payload(convertPayload(payload))
                    .addInfo1(convertPayload(addInfo1))
                    .addInfo2(convertPayload(addInfo2))
                    .build();

            userAuditTrailRepository.save(audit);

            log.info("User Audit Saved Successfully :: {}", eventType);

        } catch (Exception e) {

            log.error("Error while saving User Audit", e);

        }
    }

    /**
     * Generic Application Audit with Custom Payload
     */
    public void saveApplicationAudit(String applicationId,
                                     String customerId,
                                     String userId,
                                     String userName,
                                     String userRole,
                                     String stageId,
                                     String subStage,
                                     String wfStatus,
                                     String flag,          // "N" -> simple insert, "Y" -> insert + modified_payload diff
                                     Object payload,
                                     Object addInfo1,
                                     Object addInfo2,
                                     Object addInfo3,
                                     Object addInfo4) {

        try {

            Map<String, Object> newPayload = convertPayload(payload);
            Map<String, Object> editedDetails = null;

            if ("Y".equalsIgnoreCase(flag)) {
                // last event of same application + stage se diff nikaalo
                TbObCustAuditTrail prev = custAuditTrailRepository
                        .findFirstByApplicationIdAndStageIdOrderByCreateTsDesc(applicationId, stageId);

                if (prev != null) {
                    editedDetails = buildEditedDetails(prev.getPayload(), newPayload);
                }
            }

            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .applicationId(applicationId)
                    .customerId(customerId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .stageId(stageId)
                    .subStage(subStage)
                    .wfStatus(wfStatus)
                    .payload(newPayload)
                    .editedDetails(editedDetails)
                    .isEdited(editedDetails != null)
                    .addInfo1(convertPayload(addInfo1))
                    .addInfo2(convertPayload(addInfo2))
                    .addInfo3(convertPayload(addInfo3))
                    .addInfo4(convertPayload(addInfo4))
                    .createTs(LocalDateTime.now())
                    .build();

            custAuditTrailRepository.save(audit);
            log.info("Application Audit Saved :: stage={} flag={}", stageId, flag);

        } catch (Exception e) {
            log.error("Error while saving Application Audit", e);
        }
    }

    /**
     * Application Audit for the KM create/update flow (ApplicationServiceImpl). Diffs against the
     * previous submission of the same sub-stage (not just same stage) so re-submitting a sub-stage
     * screen (e.g. KM edits 1.2 again) is captured correctly, and carries the kendra/group/branch
     * context needed for audit-trail filtering.
     */
    public void saveApplicationAudit(TbObApplicationMaster master,
                                     TbObCustomer customer,
                                     String userId,
                                     String userName,
                                     String userRole,
                                     String appId,
                                     String appVersion,
                                     String stageId,
                                     String subStage,
                                     String wfStatus,
                                     boolean isUpdate,
                                     Object payload,
                                     Map<String, Object> addInfo1) {

        try {

            String applicationId = master.getApplicationId();
            Map<String, Object> newPayload = convertPayload(payload);
            Map<String, Object> editedDetails = null;

            if (isUpdate) {
                TbObCustAuditTrail prev = custAuditTrailRepository
                        .findFirstByApplicationIdAndSubStageOrderByCreateTsDesc(applicationId, subStage);
                if (prev != null && prev.getPayload() != null) {
                    editedDetails = buildEditedDetails(prev.getPayload(), newPayload);
                }
            }

            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .appId(appId)
                    .applicationId(applicationId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .stageId(stageId)
                    .subStage(subStage)
                    .wfStatus(wfStatus)
                    .customerId(String.valueOf(customer.getCustomerId()))
                    .customerName(customer.getCustomerName())
                    .mobileNo(
                            customer.getKycDetails() != null
                                    ? String.valueOf(customer.getKycDetails().get("mobileNum"))
                                    : master.getMobileNumber()
                    )
                    .kendraId(master.getKendraId())
                    .kendraName(master.getKendraName())
                    .groupId(master.getGroupId())
                    .branchId(master.getBranchId())
                    .payload(newPayload)
                    .editedDetails(editedDetails)
                    .isEdited(editedDetails != null)
                    .addInfo1(addInfo1)
                    .appVersion(appVersion)
                    .createTs(LocalDateTime.now())
                    .build();

            custAuditTrailRepository.save(audit);
            log.info("Application Audit Saved Successfully :: ApplicationId={}, subStage={}", applicationId, subStage);

        } catch (Exception e) {
            log.error("Error while saving Application Audit", e);
        }
    }

    /**
     * Field-level diff between the previous and current payload of a stage/sub-stage submission.
     * Shared by every caller that needs to know exactly what changed (KM re-submits, RPC edits, etc.)
     * so this logic lives in one place instead of being duplicated per service.
     */
    public Map<String, Object> computeModifiedPayload(Map<String, Object> oldMap,
                                                       Map<String, Object> newMap) {
        if (oldMap == null || newMap == null) {
            return null;
        }

        Map<String, Object> changes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> e : newMap.entrySet()) {
            Object oldVal = oldMap.get(e.getKey());
            Object newVal = e.getValue();
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> oldNew = new LinkedHashMap<>();
                oldNew.put("old", oldVal);
                oldNew.put("new", newVal);
                changes.put(e.getKey(), oldNew);
            }
        }

        return changes.isEmpty() ? null : changes;
    }

    /**
     * Count of fields that actually changed between two payloads. Callers can use this to
     * decide/display "N fields modified" without recomputing the diff themselves.
     */
    public int countModifiedFields(Map<String, Object> oldMap, Map<String, Object> newMap) {
        Map<String, Object> changes = computeModifiedPayload(oldMap, newMap);
        return changes == null ? 0 : changes.size();
    }

    /**
     * Builds the value persisted into tb_ob_cust_audit_trail.editeddetails: the per-field
     * old/new diff plus the modified field count, or null when nothing changed.
     */
    public Map<String, Object> buildEditedDetails(Map<String, Object> oldPayload, Map<String, Object> newPayload) {
        Map<String, Object> changes = computeModifiedPayload(oldPayload, newPayload);
        if (changes == null) {
            return null;
        }
        Map<String, Object> editedDetails = new LinkedHashMap<>();
        editedDetails.put("modifiedFieldCount", changes.size());
        editedDetails.put("changes", changes);
        return editedDetails;
    }
}
