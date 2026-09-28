package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplnWorkflowEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowDefinitionEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmWorkflowDefinitionRepository;

@Service
public class WorkflowEngineService {

    private static final Logger logger = LogManager.getLogger(WorkflowEngineService.class);

    private final CmWorkflowDefinitionRepository workflowDefRepo;
    private final CmApplnWorkflowRepository applnWorkflowRepo;

    public WorkflowEngineService(
            CmWorkflowDefinitionRepository workflowDefRepo,
            CmApplnWorkflowRepository applnWorkflowRepo) {
        this.workflowDefRepo = workflowDefRepo;
        this.applnWorkflowRepo = applnWorkflowRepo;
    }

    /**
     * Evaluates next workflow transition and records execution history in tb_cm_appln_workflow
     */
    @Transactional("primaryTransactionManager")
    public String transitionWorkflow(String appId, String applicationId, Integer versionNo,
                                      String currentStage, String action, String userId,
                                      String userName, String currentRole, String remarks) {
        logger.info("Evaluating workflow transition for App: {}, Current Stage: {}, Action: {}",
                applicationId, currentStage, action);

        Optional<CmWorkflowDefinitionEntity> defOpt = workflowDefRepo
                .findByAppIdAndWorkflowIdAndFromStageIdAndAction(appId, "CM_UPDATE_FLOW", currentStage, action);

        String nextStage = defOpt.map(CmWorkflowDefinitionEntity::getNextStageId).orElse("COMPLETED");
        String nextRole = defOpt.map(CmWorkflowDefinitionEntity::getNextRole).orElse(null);
        String nextStatus = defOpt.map(CmWorkflowDefinitionEntity::getNextWorkflowStatus).orElse("APPROVED");

        int nextSeqNo = applnWorkflowRepo.findMaxSequenceNo(applicationId).orElse(0) + 1;

        CmApplnWorkflowEntity history = CmApplnWorkflowEntity.builder()
                .appId(appId)
                .applicationId(applicationId)
                .versionNo(versionNo != null ? versionNo : 1)
                .workflowSeqNo(nextSeqNo)
                .applicationStatus(nextStatus)
                .createdTs(LocalDateTime.now())
                .createdBy(userId)
                .presentRole(currentRole)
                .nextWorkflowStage(nextStage)
                .remarks(remarks)
                .createdUsername(userName)
                .build();

        applnWorkflowRepo.save(history);
        logger.info("Transitioned App ID: {} to Next Stage: {}, Next Role: {}", applicationId, nextStage, nextRole);
        return nextStage;
    }
}
