package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.client.CbsSyncClient;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplnWorkflowEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowDefinitionEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowHistoryDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowQueueItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowTransitionRequest;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmWorkflowDefinitionRepository;

@Service
public class WorkflowEngineService {

    private static final Logger logger = LogManager.getLogger(WorkflowEngineService.class);
    private static final String DEFAULT_APP_ID = "APZCBO";

    private final CmWorkflowDefinitionRepository workflowDefRepo;
    private final CmApplnWorkflowRepository applnWorkflowRepo;
    private final CmApplicationMasterRepository appRepo;
    private final CbsSyncClient cbsClient;

    public WorkflowEngineService(
            CmWorkflowDefinitionRepository workflowDefRepo,
            CmApplnWorkflowRepository applnWorkflowRepo,
            CmApplicationMasterRepository appRepo,
            CbsSyncClient cbsClient) {
        this.workflowDefRepo = workflowDefRepo;
        this.applnWorkflowRepo = applnWorkflowRepo;
        this.appRepo = appRepo;
        this.cbsClient = cbsClient;
    }

    /**
     * Executes dynamic workflow transition following the team lead matrix
     */
    @Transactional("primaryTransactionManager")
    public String transitionWorkflow(WorkflowTransitionRequest request, RequestHeader header) {
        String appId = request.getAppId() != null ? request.getAppId() : DEFAULT_APP_ID;
        String applicationId = request.getApplicationId();
        String currentStage = request.getCurrentStage() != null ? request.getCurrentStage() : "INITIATE";
        String requestedAction = request.getAction() != null ? request.getAction() : "SUBMIT";

        String userId = header != null && header.getUserId() != null ? header.getUserId() : "SYSTEM";
        String userName = header != null && header.getUserId() != null ? header.getUserId() : "SYSTEM";
        String userRole = header != null && header.getUserRole() != null ? header.getUserRole() : "KM";

        if (request.getInitiatorRole() != null && !request.getInitiatorRole().isBlank()) {
            userRole = request.getInitiatorRole();
        }

        // 1. Resolve workflow_id from current stage and user role
        String workflowId = resolveWorkflowId(currentStage, userRole);

        // 2. Resolve evaluated action (e.g. STP submit vs normal submit, RPC 4-eyes check, BRE decisions)
        String evaluatedAction = resolveEvaluatedAction(currentStage, requestedAction, request);

        logger.info("Evaluating Transition -> AppId: {}, WorkflowId: {}, FromStage: {}, Action: {} (Original: {})",
                appId, workflowId, currentStage, evaluatedAction, requestedAction);

        // 3. Look up transition definition
        Optional<CmWorkflowDefinitionEntity> defOpt = workflowDefRepo
                .findByAppIdAndWorkflowIdAndFromStageIdAndAction(appId, workflowId, currentStage, evaluatedAction);

        String nextStage;
        String nextRole;
        String nextStatus;

        if (defOpt.isPresent()) {
            CmWorkflowDefinitionEntity def = defOpt.get();
            nextStage = def.getNextStageId();
            nextRole = def.getNextRole();
            nextStatus = def.getNextWorkflowStatus();
        } else {
            logger.warn("No transition rule found for [{}, {}, {}, {}]. Falling back to direct action mapping.",
                    appId, workflowId, currentStage, evaluatedAction);
            nextStage = evaluatedAction.equals("APPROVED") ? "COMPLETED" : "DRAFT";
            nextRole = userRole;
            nextStatus = "IN_PROGRESS";
        }

        // 4. Update Application Master record
        Optional<CmApplicationMasterEntity> appOpt = appRepo.findByApplicationId(applicationId);
        int versionNo = 1;
        if (appOpt.isPresent()) {
            CmApplicationMasterEntity app = appOpt.get();
            app.setStage(nextStage);
            app.setWfstage(nextStage);
            app.setStatus(nextStatus != null ? nextStatus : app.getStatus());
            app.setUpdatedBy(userId);
            app.setUpdatedTs(LocalDateTime.now());
            appRepo.save(app);
            try {
                versionNo = Integer.parseInt(app.getVersion());
            } catch (Exception ignored) {
            }
        }

        // 5. Record workflow audit trail
        int nextSeqNo = applnWorkflowRepo.findMaxSequenceNo(applicationId).orElse(0) + 1;
        CmApplnWorkflowEntity history = CmApplnWorkflowEntity.builder()
                .appId(appId)
                .applicationId(applicationId)
                .versionNo(versionNo)
                .workflowSeqNo(nextSeqNo)
                .applicationStatus(nextStatus)
                .createdTs(LocalDateTime.now())
                .createdBy(userId)
                .presentRole(userRole)
                .nextWorkflowStage(nextStage)
                .remarks(request.getRemarks() != null ? request.getRemarks() : "Transition to " + nextStage)
                .createdUsername(userName)
                .build();
        applnWorkflowRepo.save(history);

        logger.info("Transitioned Application: {} -> Next Stage: {}, Next Role: {}, Status: {}",
                applicationId, nextStage, nextRole, nextStatus);

        // 6. Automated Pipeline Processing (AML/BRE/T24)
        handleAutomatedStages(appId, applicationId, versionNo, nextStage, request, header);

        return nextStage;
    }

    /**
     * Resolves the Workflow ID based on stage and role
     */
    private String resolveWorkflowId(String currentStage, String role) {
        String stageUpper = currentStage.toUpperCase();
        String roleUpper = role.toUpperCase();

        if (stageUpper.equals("INITIATE") || stageUpper.equals("DRAFT") ||
            stageUpper.equals("BMONHOLD") || stageUpper.equals("RPCONHOLD") || stageUpper.equals("AMONHOLD")) {
            return switch (roleUpper) {
                case "DEO" -> "CMDEOINPUT";
                case "BM" -> "CMBMINPUT";
                case "AM" -> "CMAMINPUT";
                default -> "CMKMINPUT";
            };
        } else if (stageUpper.equals("BMQUEUE")) {
            return "CMBMINPUT";
        } else if (stageUpper.equals("AMQUEUE")) {
            return "CMAMINPUT";
        } else if (stageUpper.equals("RPCMAKERQUEUE")) {
            return "CMRPCMAKER";
        } else if (stageUpper.equals("RPCCHECKERQUEUE")) {
            return "CMRPCCHECKER";
        } else if (stageUpper.equals("AMLQUEUE")) {
            return "CMAMLHO";
        } else if (stageUpper.equals("CRTQUEUE")) {
            return "CMCRT";
        } else if (stageUpper.equals("INSQUEUE")) {
            return "CMINSURANCE";
        } else if (stageUpper.equals("T24PENDING")) {
            return roleUpper.contains("CHT") ? "CMCHT" : "CMSYSTEM";
        } else if (stageUpper.equals("COMPLETED")) {
            return roleUpper.contains("HO") ? "CMRPCHO" : "CMRPCTL";
        } else if (stageUpper.equals("BSTQUEUE")) {
            return "CMBST";
        } else {
            return "CMSYSTEM";
        }
    }

    /**
     * Resolves dynamic action based on business rules (STP, 4-Eyes, BRE decisions)
     */
    private String resolveEvaluatedAction(String currentStage, String action, WorkflowTransitionRequest request) {
        String stageUpper = currentStage.toUpperCase();
        String actionUpper = action.toUpperCase();

        // Rule 1: STP vs Non-STP on Draft Submit
        if (stageUpper.equals("DRAFT") && actionUpper.equals("SUBMIT")) {
            if (Boolean.FALSE.equals(request.getIsKycEdited())) {
                return "STPSUBMIT"; // Green Channel / Fast Track
            }
            return "SUBMIT";
        }

        // Rule 2: RPC Maker 4-Eyes Principle (if RPC Maker modified fields -> moves to RPC Checker)
        if (stageUpper.equals("RPCMAKERQUEUE") && actionUpper.equals("APPROVED")) {
            if (Boolean.TRUE.equals(request.getIsRpcEdited())) {
                return "CHECKER"; // Edited -> Checker Queue
            }
            return "APPROVED"; // Unedited -> Direct Approval
        }

        // Rule 3: AML Trigger Evaluation
        if (stageUpper.equals("RPCAPPROVED") || stageUpper.equals("STPSUBMITTED")) {
            if (Boolean.TRUE.equals(request.getIsAmlTriggered())) {
                return "AML";
            }
            return "BRE";
        }

        // Rule 4: BRE Decision Evaluation
        if (stageUpper.equals("BREQUEUE")) {
            String bre = request.getBreDecision() != null ? request.getBreDecision().toUpperCase() : "PASS";
            if (bre.equals("PASS")) {
                if (Boolean.TRUE.equals(request.getIsWidowedToMarried())) {
                    return "INSURANCE"; // Widowed -> Married trigger
                }
                return "PASS";
            } else if (bre.equals("FAIL")) {
                return "FAIL";
            } else {
                return "QUEUE"; // Retry
            }
        }

        return actionUpper;
    }

    /**
     * Handles automated backend steps like BRE and T24 Sync
     */
    private void handleAutomatedStages(String appId, String applicationId, int versionNo,
                                       String nextStage, WorkflowTransitionRequest request, RequestHeader header) {
        if ("STPSUBMITTED".equalsIgnoreCase(nextStage) || "RPCAPPROVED".equalsIgnoreCase(nextStage)) {
            // Auto advance from STP or RPC Approved to AML / BRE
            String systemAction = Boolean.TRUE.equals(request.getIsAmlTriggered()) ? "AML" : "BRE";
            transitionDirect(appId, applicationId, versionNo, nextStage, systemAction, "CMSYSTEM", "Automated AML/BRE Evaluation");
        } else if ("T24UPDATE".equalsIgnoreCase(nextStage)) {
            // Push update to Core Banking
            boolean t24Success = cbsClient.syncToT24(request.getCustomerId(), null);
            String t24Action = t24Success ? "NEXT" : "QUEUE";
            transitionDirect(appId, applicationId, versionNo, "T24UPDATE", t24Action, "CMSYSTEM",
                    t24Success ? "T24 update success -> COMPLETED" : "T24 down -> Moved to T24PENDING");
        }
    }

    private void transitionDirect(String appId, String applicationId, int versionNo,
                                  String fromStage, String action, String workflowId, String remarks) {
        Optional<CmWorkflowDefinitionEntity> defOpt = workflowDefRepo
                .findByAppIdAndWorkflowIdAndFromStageIdAndAction(appId, workflowId, fromStage, action);
        if (defOpt.isPresent()) {
            CmWorkflowDefinitionEntity def = defOpt.get();
            String nextStage = def.getNextStageId();
            String nextStatus = def.getNextWorkflowStatus();

            appRepo.findByApplicationId(applicationId).ifPresent(app -> {
                app.setStage(nextStage);
                app.setWfstage(nextStage);
                app.setStatus(nextStatus);
                app.setUpdatedBy("SYSTEM");
                app.setUpdatedTs(LocalDateTime.now());
                appRepo.save(app);
            });

            int nextSeqNo = applnWorkflowRepo.findMaxSequenceNo(applicationId).orElse(0) + 1;
            CmApplnWorkflowEntity history = CmApplnWorkflowEntity.builder()
                    .appId(appId)
                    .applicationId(applicationId)
                    .versionNo(versionNo)
                    .workflowSeqNo(nextSeqNo)
                    .applicationStatus(nextStatus)
                    .createdTs(LocalDateTime.now())
                    .createdBy("SYSTEM")
                    .presentRole("SYSTEM")
                    .nextWorkflowStage(nextStage)
                    .remarks(remarks)
                    .createdUsername("System Automator")
                    .build();
            applnWorkflowRepo.save(history);
        }
    }

    /**
     * Fetches queue tasks pending for a specific role or stage
     */
    @Transactional(readOnly = true)
    public List<WorkflowQueueItemDto> getQueueItems(String role, String stage, String branchId) {
        List<String> targetStages = new ArrayList<>();
        if (stage != null && !stage.isBlank()) {
            targetStages.add(stage);
        } else if (role != null) {
            switch (role.toUpperCase()) {
                case "KM", "DEO" -> {
                    targetStages.add("DRAFT");
                    targetStages.add("BMONHOLD");
                    targetStages.add("RPCONHOLD");
                }
                case "BM" -> {
                    targetStages.add("BMQUEUE");
                    targetStages.add("AMONHOLD");
                }
                case "AM" -> targetStages.add("AMQUEUE");
                case "RPCMAKER" -> targetStages.add("RPCMAKERQUEUE");
                case "RPCCHECKER" -> targetStages.add("RPCCHECKERQUEUE");
                case "AMLHO" -> targetStages.add("AMLQUEUE");
                case "CRT" -> targetStages.add("CRTQUEUE");
                case "INSURANCE" -> targetStages.add("INSQUEUE");
                case "CHT" -> targetStages.add("T24PENDING");
                case "RPCTL", "RPCHO" -> targetStages.add("COMPLETED");
                case "BST" -> targetStages.add("BSTQUEUE");
                default -> targetStages.add("DRAFT");
            }
        }

        List<CmApplicationMasterEntity> apps = targetStages.isEmpty()
                ? appRepo.findAll()
                : appRepo.findByStageIn(targetStages);

        if (branchId != null && !branchId.isBlank()) {
            apps = apps.stream().filter(a -> branchId.equalsIgnoreCase(a.getBranchId())).collect(Collectors.toList());
        }

        return apps.stream().map(a -> WorkflowQueueItemDto.builder()
                .applicationId(a.getApplicationId())
                .customerId(a.getCustomerId())
                .customerName(a.getCustomerName())
                .mobileNumber(a.getMobileNumber())
                .branchId(a.getBranchId())
                .branchName(a.getBranchName())
                .kendraId(a.getKendraId())
                .kendraName(a.getKendraName())
                .kmName(a.getKmName())
                .stage(a.getStage())
                .status(a.getStatus())
                .pendingRole(role)
                .createdTs(a.getCreatedTs())
                .updatedTs(a.getUpdatedTs())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * Fetches complete movement history for an application
     */
    @Transactional(readOnly = true)
    public List<WorkflowHistoryDto> getApplicationWorkflowHistory(String applicationId) {
        return applnWorkflowRepo.findByApplicationIdOrderByWorkflowSeqNoDesc(applicationId).stream()
                .map(h -> WorkflowHistoryDto.builder()
                        .applicationId(h.getApplicationId())
                        .versionNo(h.getVersionNo())
                        .workflowSeqNo(h.getWorkflowSeqNo())
                        .applicationStatus(h.getApplicationStatus())
                        .presentRole(h.getPresentRole())
                        .nextWorkflowStage(h.getNextWorkflowStage())
                        .remarks(h.getRemarks())
                        .createdBy(h.getCreatedBy())
                        .createdUsername(h.getCreatedUsername())
                        .createdTs(h.getCreatedTs())
                        .build())
                .collect(Collectors.toList());
    }
}
