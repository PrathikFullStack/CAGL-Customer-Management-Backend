package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.constants.AuditConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustAuditTrail;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLead;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObUserAuditTrail;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailRecord;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObUserAuditTrailRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.CustAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Maps a dashboard search request's searchType (global / local / filter) to its audit event type.
     */
    public String resolveSearchEventType(String searchType) {
        if ("FILTER".equalsIgnoreCase(searchType)) {
            return AuditConstants.FILTER_SEARCH;
        }
        if ("LOCAL".equalsIgnoreCase(searchType)) {
            return AuditConstants.LOCAL_SEARCH;
        }
        return AuditConstants.GLOBAL_SEARCH;
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
     * Serialize an object to a JSON string, for the plain-text payload/editeddetails/add_info columns
     * on tb_ob_cust_audit_trail (not jsonb).
     */
    private String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error converting object to JSON string", e);
            return null;
        }
    }

    /**
     * Parse a JSON string column back into a Map for the API response.
     */
    private Map<String, Object> parseJsonToMap(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("Error parsing JSON string to map", e);
            return null;
        }
    }
    /**
     * Fetch Application Audit Trail by ApplicationId
     */
    public List<AuditTrailRecord> fetchApplicationAuditTrail(String applicationId,
                                                              String userId,
                                                              String userRole) {

        if (!StringUtils.hasText(applicationId)) {
            return new ArrayList<>();
        }
        return custAuditTrailRepository.findByApplicationId(applicationId).stream()
                .map(this::convertApplicationAudit)
                .sorted(Comparator.comparing(AuditTrailRecord::getEventTimestamp).reversed())
                .collect(Collectors.toList());
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
                .id(audit.getId())
                .eventTimestamp(audit.getCreateTs())
                .userId(audit.getUserId())
                .userName(audit.getUserName())
                .userRole(audit.getUserRole())
                .details(details.toString())
                .editedDetails(parseJsonToMap(audit.getEditedDetails()))
                .payload(parseJsonToMap(audit.getPayload()))
                .isEdited("true".equalsIgnoreCase(audit.getIsEdited()))
                .build();
    }
    /**
     * Application Audit
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
                                     Object payload,
                                     Map<String, Object> modifiedDetails,
                                     Map<String, Object> addInfo1) {

        try {

            String applicationId = master.getApplicationId();
            Map<String, Object> editedDetails = flattenModifiedDetails(modifiedDetails);

            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .id(String.valueOf(custAuditTrailRepository.getNextAuditTrailId()))
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
                    .payload(toJsonString(payload))
                    .editedDetails(toJsonString(editedDetails))
                    .isEdited(editedDetails != null ? "true" : "false")
                    .addInfo1(toJsonString(addInfo1))
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
     * Application Audit for a real workflow stage transition (ApplicationWorkflowService.updateStage()
     */
    public void saveStageTransitionAudit(TbObApplicationMaster master,
                                          String userId,
                                          String userName,
                                          String userRole,
                                          String appId,
                                          String appVersion,
                                          String stageId,
                                          String wfStatus,
                                          Object payload) {

        try {
            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .id(String.valueOf(custAuditTrailRepository.getNextAuditTrailId()))
                    .appId(appId)
                    .applicationId(master.getApplicationId())
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .stageId(stageId)
                    .wfStatus(wfStatus)
                    .customerId(master.getCustomerId())
                    .customerName(master.getCustomerName())
                    .mobileNo(master.getMobileNumber())
                    .kendraId(master.getKendraId())
                    .kendraName(master.getKendraName())
                    .groupId(master.getGroupId())
                    .branchId(master.getBranchId())
                    .payload(toJsonString(payload))
                    .appVersion(appVersion)
                    .createTs(LocalDateTime.now())
                    .build();

            custAuditTrailRepository.save(audit);
            log.info("Stage Transition Audit Saved Successfully :: ApplicationId={}, stageId={}, wfStatus={}",
                    master.getApplicationId(), stageId, wfStatus);

        } catch (Exception e) {
            log.error("Error while saving Stage Transition Audit", e);
        }
    }

    /**
     * Application Audit for a Lead (tb_ob_cust_audit_trail). No application exists yet at lead stage,
    */
    public void saveApplicationLeadAudit(TbObLead lead, String userId, String eventType) {

        try {
            TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                    .id(String.valueOf(custAuditTrailRepository.getNextAuditTrailId()))
                    .applicationId(lead.getLeadId())
                    .userId(userId)
                    .stageId(eventType)
                    .wfStatus(lead.getStatus())
                    .customerName(lead.getCustomerName())
                    .mobileNo(lead.getMobileNumber())
                    .kendraId(lead.getKendraId())
                    .branchId(lead.getBranchId())
                    .payload(toJsonString(lead))
                    .createTs(LocalDateTime.now())
                    .build();
            custAuditTrailRepository.save(audit);
            log.info("Application Audit Saved Successfully for Lead :: LeadId={}, eventType={}", lead.getLeadId(), eventType);

        } catch (Exception e) {
            log.error("Error while saving Application Audit for Lead", e);
        }
    }

    private Map<String, Object> flattenModifiedDetails(Map<String, Object> modifiedDetails) {
        if (modifiedDetails == null || modifiedDetails.isEmpty()) {
            return null;
        }
        Map<String, Object> flat = new LinkedHashMap<>();
        flattenPayload("", modifiedDetails, flat);
        return flat.isEmpty() ? null : flat;
    }


    @SuppressWarnings("unchecked")
    private void flattenPayload(String pathPrefix, Object node, Map<String, Object> out) {
        if (node instanceof Map) {
            ((Map<String, Object>) node).forEach((key, value) -> {
                String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
                flattenPayload(path, value, out);
            });
        } else if (node instanceof List) {
            List<Object> list = (List<Object>) node;
            for (int i = 0; i < list.size(); i++) {
                flattenPayload(pathPrefix + "[" + i + "]", list.get(i), out);
            }
        } else if (node != null) {
            out.put(pathPrefix, node);
        }
    }

}
