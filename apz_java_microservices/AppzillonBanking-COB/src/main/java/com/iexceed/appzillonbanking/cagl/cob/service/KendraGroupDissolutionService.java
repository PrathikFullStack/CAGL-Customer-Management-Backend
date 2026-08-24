package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.KendraGroupDissolveRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.KendraGroupDissolveRequestFields;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;

import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;

import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class KendraGroupDissolutionService {

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    private TbObGroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

//    @Value("${onboarding.KendraGroupDissolution.auto-dissolution-inactive-days:90}")
//    private int autoDissolutionInactiveDays;

    @Value("${ab.common.groupDissolutionDays:90}")
    private Integer groupDissolutionDays;

    @Value("${ab.common.kendraDissolutionDays:90}")
    private Integer kendraDissolutionDays;

//    @Value("${onboarding.KendraGroupDissolution.guard-stages:}")
//    private String guardStagesCsv; -->> no need this gaurd stages validation

    private static final Logger logger = LogManager.getLogger(KendraGroupDissolutionService.class);

    private static final String TRIGGER_BY_SCHEDULER = "SCHEDULER";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISSOLVED = "DISSOLVED";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String REASON_AUTO_INACTIVE = "AUTO_INACTIVE_DISSOLUTION";
    private static final String REASON_MANUAL = "MANUAL_DISSOLUTION";
    private static final List<String> KENDRA_CANDIDATE_STATUSES = List.of("ACTIVE", "PENDING");

    @Transactional
    public Mono<Response> dissolveKendraGroup(KendraGroupDissolveRequest request, Header header) {

        logger.info("Kendra-Group Dissolve API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            KendraGroupDissolveRequestFields requestObj = request.getRequestObj();
            String userId = request.getUserId();

            List<String> kendraIds = requestObj.getKendraIds();
            List<String> groupIds = requestObj.getGroupIds();

            Map<String, Object> result = new LinkedHashMap<>();

            if (TRIGGER_BY_SCHEDULER.equalsIgnoreCase(requestObj.getTriggerType())) {
                // Scheduler trigger: ignores whatever IDs were sent (if any) and checks everything itself.
                result.put("schedulerResult", dissolveInactive());
            } else {
                // Someone acting on a specific Kendra/Group by ID: no member-count, age, or
                // guard-stage validation at all -- the caller named it, so it's set INACTIVE outright.
                if (kendraIds != null && !kendraIds.isEmpty()) {
                    result.put("kendraResult", dissolveKendrasNoValidation(kendraIds, requestObj.getReason(), userId));
                }
                if (groupIds != null && !groupIds.isEmpty()) {
                    result.put("groupResult", dissolveGroupsNoValidation(groupIds, requestObj.getReason(), userId));
                }
            }

            responseBody.setResponseObj(objectMapper.writeValueAsString(result));
            CommonUtils.generateHeaderForSuccess(responseHeader);
            logger.info("Kendra-Group Dissolve API completed.");

        } catch (Exception ex) {
            logger.error("Exception while dissolving Kendra/Group.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /**
     * SCHEDULER trigger only: finds every ACTIVE zero-member group and every ACTIVE/PENDING
     * zero-group Kendra past its own configured inactivity cutoff, and dissolves all of them.
     * This is the only path that checks anything -- {@code dissolveKendrasNoValidation}/
     * {@code dissolveGroupsNoValidation} trust the caller completely.
     */
    private Map<String, Object> dissolveInactive() {

        logger.debug("========= Inside Dissolution Scheduler =========");

        int groupDays = (groupDissolutionDays == null) ? 90 : groupDissolutionDays;
        int kendraDays = (kendraDissolutionDays == null) ? 90 : kendraDissolutionDays;

        LocalDateTime groupCutoff = LocalDateTime.now().minusDays(groupDays);
        LocalDateTime kendraCutoff = LocalDateTime.now().minusDays(kendraDays);

        List<String> dissolvedGroupIds = new ArrayList<>();
        List<String> dissolvedKendraIds = new ArrayList<>();

        // ── 1. Groups : ACTIVE, zero members, created before cutoff ──
        List<TbObGroup> staleGroups =
                groupRepository.findByStatusAndTotalMemberCountAndCreatedTsBefore(
                        STATUS_ACTIVE,
                        String.valueOf(0),
                        groupCutoff);
        if (staleGroups.isEmpty()) {
            logger.debug("No stale groups found for dissolution");
        }
        for (TbObGroup group : staleGroups) {
            try {
                logger.debug("Dissolving groupId :: {}", group.getGroupId());
                group.setStatus(STATUS_DISSOLVED);
                group.setDissolutionReason(REASON_AUTO_INACTIVE);
                group.setDissolutionTs(LocalDateTime.now());
                group.setUpdatedBy(TRIGGER_BY_SCHEDULER);
                group.setUpdatedTs(LocalDateTime.now());
                groupRepository.save(group);

                // TODO: raise GROUP_DISSOLVED audit trail event once audit storage is finalized.
                dissolvedGroupIds.add(group.getGroupId());
                logger.debug("Group dissolved :: {}", group.getGroupId());
            } catch (Exception ex) {
                logger.error("Dissolution failed for groupId :: {}", group.getGroupId(), ex);
            }
        }

        // ── 2. Kendras : ACTIVE/PENDING, no groups, created before cutoff ──
        List<TbObKendra> candidateKendras =
                kendraRepository.findByStatusInAndCreatedTsBefore(KENDRA_CANDIDATE_STATUSES, kendraCutoff);
        if (candidateKendras.isEmpty()) {
            logger.debug("No candidate Kendras found for dissolution");
        }
        for (TbObKendra kendra : candidateKendras) {
            try {
                // Skip if the Kendra has any groups at all
                long groupCount = groupRepository.countByKendraId(kendra.getKendraId());
                if (groupCount > 0) {
                    continue;
                }
                logger.debug("Dissolving kendraId :: {}", kendra.getKendraId());
                kendra.setStatus(STATUS_DISSOLVED);
                kendra.setUpdatedBy(TRIGGER_BY_SCHEDULER);
                kendra.setUpdatedTs(LocalDateTime.now());
                kendraRepository.save(kendra);

                // TODO: raise KENDRA_DISSOLVED audit trail event once audit storage is finalized.
                dissolvedKendraIds.add(kendra.getKendraId());
                logger.debug("Kendra dissolved :: {}", kendra.getKendraId());
            } catch (Exception ex) {
                logger.error("Dissolution failed for kendraId :: {}", kendra.getKendraId(), ex);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dissolvedGroupIds", dissolvedGroupIds);
        result.put("dissolvedKendraIds", dissolvedKendraIds);
        result.put("totalDissolved", dissolvedGroupIds.size() + dissolvedKendraIds.size());
        return result;
    }

    /** Non-scheduler trigger: no checks -- the caller named this Kendra, so it's set INACTIVE outright. */
    private Map<String, Object> dissolveKendrasNoValidation(List<String> kendraIds, String reason, String userId) {

        List<String> dissolved = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        long nowMillis = System.currentTimeMillis();

        for (String kendraId : kendraIds) {
            try {
                Optional<TbObKendra> kendraOpt = kendraRepository.findByKendraId(kendraId);
                if (kendraOpt.isEmpty()) {
                    skipped.add(skipEntry(kendraId, "KENDRA_NOT_FOUND"));
                    continue;
                }

                TbObKendra kendra = kendraOpt.get();
                kendra.setStatus(STATUS_INACTIVE);
                kendra.setUpdatedBy(userId);
                kendra.setUpdatedTs(LocalDateTime.now());
                kendraRepository.save(kendra);
                dissolved.add(kendraId);

                // TODO: raise KENDRA_DISSOLVED audit trail event once audit storage is finalized.
                logger.info("Kendra Id : {} set INACTIVE (no validation).", kendraId);
            } catch (Exception ex) {
                logger.error("Dissolution failed for kendraId :: {}", kendraId, ex);
                skipped.add(skipEntry(kendraId, "PROCESSING_ERROR"));
            }
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dissolved", dissolved);
        map.put("skipped", skipped);
        return map;
    }

    /** Non-scheduler trigger: no checks -- the caller named this Group, so it's set INACTIVE outright. */
    private Map<String, Object> dissolveGroupsNoValidation(List<String> groupIds, String reason, String userId) {

        List<String> dissolved = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();

        for (String groupId : groupIds) {
            try {
                Optional<TbObGroup> groupOpt = groupRepository.findByGroupId(groupId);
                if (groupOpt.isEmpty()) {
                    skipped.add(skipEntry(groupId, "GROUP_NOT_FOUND"));
                    continue;
                }

                TbObGroup group = groupOpt.get();
                group.setStatus(STATUS_INACTIVE);
                group.setDissolutionTs(LocalDateTime.now());
                group.setDissolutionReason(reason != null ? reason : REASON_MANUAL);
                group.setUpdatedBy(userId);
                group.setUpdatedTs(LocalDateTime.now());
                groupRepository.save(group);
                dissolved.add(groupId);

                // TODO: raise GROUP_DISSOLVED audit trail event once audit storage is finalized.
                logger.info("Group Id : {} set INACTIVE (no validation).", groupId);
            } catch (Exception ex) {
                logger.error("Dissolution failed for groupId :: {}", groupId, ex);
                skipped.add(skipEntry(groupId, "PROCESSING_ERROR"));
            }
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dissolved", dissolved);
        map.put("skipped", skipped);
        return map;
    }

    private Map<String, Object> skipEntry(String id, String reasonCode) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("reason", reasonCode);
        return entry;
    }
}