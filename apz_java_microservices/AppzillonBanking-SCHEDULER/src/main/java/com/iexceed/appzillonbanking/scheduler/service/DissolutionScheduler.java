package com.iexceed.appzillonbanking.scheduler.service;

import com.iexceed.appzillonbanking.scheduler.domain.ab.SchedulerAuditLog;
import com.iexceed.appzillonbanking.scheduler.domain.ab.TbObGroup;
import com.iexceed.appzillonbanking.scheduler.domain.ab.TbObKendra;
import com.iexceed.appzillonbanking.scheduler.repository.ab.SchedulerAuditLogRepository;
import com.iexceed.appzillonbanking.scheduler.repository.ab.TbObGroupRepository;
import com.iexceed.appzillonbanking.scheduler.repository.ab.TbObKendraRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DissolutionScheduler {

    private static final Logger logger = LogManager.getLogger(DissolutionScheduler.class.getName());

    @Autowired
    private TbObGroupRepository tbObGroupRepo;

    @Autowired
    private TbObKendraRepository tbObKendraRepo;

    @Autowired
    private SchedulerAuditLogRepository auditLogRepository;

    /** Days a group can stay with zero members before auto-dissolution. Default 90. */
    @Value("${ab.common.groupDissolutionDays:90}")
    private Integer groupDissolutionDays;

    /** Days a Kendra can stay with zero groups before auto-dissolution. Default 90. */
    @Value("${ab.common.kendraDissolutionDays:90}")
    private Integer kendraDissolutionDays;

    @Scheduled(cron = "${ab.common.dissolutionSchedulerCron}")
    public void scheduleTask() {SchedulerAuditLog auditLog = new SchedulerAuditLog();
        LocalDateTime startTime = LocalDateTime.now();
        auditLog.setSchedulerName("DissolutionScheduler");
        auditLog.setStartTime(startTime);
        auditLog.setCreatedAt(startTime);
        try {
            logger.debug("Dissolution Scheduler started at {}", startTime);
            int count = dissolveInactive(auditLog);
            auditLog.setStatus("SUCCESS");
            auditLog.setRecordCount(count);
        } catch (Exception e) {
            logger.error("Exception in DissolutionScheduler", e);
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(e.getMessage());
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            auditLog.setEndTime(endTime);
            auditLogRepository.save(auditLog);
            logger.debug("Dissolution Scheduler ended at {}", endTime);
        }
    }

    private int dissolveInactive(SchedulerAuditLog auditLog) {
        logger.debug("========= Inside Dissolution Scheduler =========");
        int groupDays  = (groupDissolutionDays  == null) ? 90 : groupDissolutionDays;
        int kendraDays = (kendraDissolutionDays == null) ? 90 : kendraDissolutionDays;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime groupCutoff  = now.minusDays(groupDays);
        LocalDateTime kendraCutoff = now.minusDays(kendraDays);
        List<String> processedIds = new ArrayList<>();
        int dissolvedCount = 0;
        // ── 1. Groups : ACTIVE, zero members, created before cutoff ──
        List<TbObGroup> staleGroups =
                tbObGroupRepo.findByStatusAndMemberCountAndCreatedTsBefore("ACTIVE", 0, groupCutoff);
        if (staleGroups.isEmpty()) {
            logger.debug("No stale groups found for dissolution");
        }
        for (TbObGroup group : staleGroups) {
            try {
                logger.debug("Dissolving groupId :: {}", group.getGroupId());
                processedIds.add("G:" + group.getGroupId());
                group.setStatus("DISSOLVED");
                group.setDissolutionReason("AUTO_INACTIVE");
                group.setDissolutionTs(now);
                group.setUpdatedBy("SCHEDULER");
                group.setUpdatedTs(now);
                tbObGroupRepo.save(group);

                dissolvedCount++;
                logger.debug("Group dissolved :: {}", group.getGroupId());
            } catch (Exception ex) {
                logger.error("Dissolution failed for groupId :: {}", group.getGroupId(), ex);
            }
        }
        // ── 2. Kendras : ACTIVE/PENDING, no groups, created before cutoff ──
        List<TbObKendra> candidateKendras =
                tbObKendraRepo.findByStatusInAndCreatedTsBefore(
                        List.of("ACTIVE", "PENDING"), kendraCutoff);
        if (candidateKendras.isEmpty()) {
            logger.debug("No candidate Kendras found for dissolution");
        }
        for (TbObKendra kendra : candidateKendras) {
            try {
                // Skip if the Kendra has any groups at all
                long groupCount = tbObGroupRepo.countByKendraId(kendra.getKendraId());
                if (groupCount > 0) {
                    continue;
                }
                logger.debug("Dissolving kendraId :: {}", kendra.getKendraId());
                processedIds.add("K:" + kendra.getKendraId());
                kendra.setStatus("REJECTED");
                kendra.setUpdatedBy("SCHEDULER");
                kendra.setUpdatedTs(now);
                tbObKendraRepo.save(kendra);
                dissolvedCount++;
                logger.debug("Kendra dissolved :: {}", kendra.getKendraId());
            } catch (Exception ex) {
                logger.error("Dissolution failed for kendraId :: {}", kendra.getKendraId(), ex);
            }
        }
        auditLog.setProcessedIds(String.join(",", processedIds));
        return dissolvedCount;
    }
}
