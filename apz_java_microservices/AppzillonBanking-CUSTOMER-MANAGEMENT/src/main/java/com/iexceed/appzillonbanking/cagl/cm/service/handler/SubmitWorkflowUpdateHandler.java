package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.client.CbsSyncClient;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.RecordLockService;
import com.iexceed.appzillonbanking.cagl.cm.service.WorkflowEngineService;

@Component
public class SubmitWorkflowUpdateHandler implements UpdateHandler {

    private final CmApplicationMasterRepository appRepo;
    private final WorkflowEngineService workflowEngine;
    private final RecordLockService lockService;
    private final CbsSyncClient cbsClient;

    public SubmitWorkflowUpdateHandler(
            CmApplicationMasterRepository appRepo,
            WorkflowEngineService workflowEngine,
            RecordLockService lockService,
            CbsSyncClient cbsClient) {
        this.appRepo = appRepo;
        this.workflowEngine = workflowEngine;
        this.lockService = lockService;
        this.cbsClient = cbsClient;
    }

    @Override
    public String getSectionName() {
        return "SUBMIT";
    }

    @Override
    @Transactional("primaryTransactionManager")
    public UpdateResponseDto handleUpdate(CustomerUpdateRequest request, RequestHeader header) {
        Optional<CmApplicationMasterEntity> appOpt = appRepo.findByApplicationId(request.getApplicationId());
        if (appOpt.isEmpty()) {
            return UpdateResponseDto.builder()
                    .applicationId(request.getApplicationId())
                    .customerId(request.getCustomerId())
                    .section(getSectionName())
                    .status("FAILED")
                    .build();
        }

        CmApplicationMasterEntity app = appOpt.get();
        String currentStage = app.getStage() != null ? app.getStage() : "KM_DRAFT";
        String action = "SUBMIT";

        // Transition workflow state
        String nextStage = workflowEngine.transitionWorkflow("APZ_CM", app.getApplicationId(),
                Integer.parseInt(app.getVersion()), currentStage, action,
                header != null ? header.getUserId() : "SYSTEM",
                header != null ? header.getUserId() : "SYSTEM",
                header != null ? header.getUserRole() : "KM",
                request.getRemarks());

        app.setStage(nextStage);
        appRepo.save(app);

        // If auto-approved (STP), push to T24 Core Banking
        if ("STP_APPROVED".equalsIgnoreCase(nextStage) || "COMPLETED".equalsIgnoreCase(nextStage)) {
            cbsClient.syncToT24(app.getCustomerId(), request.getUpdatePayload());
        }

        // Release lock
        lockService.releaseLock(app.getApplicationId(), header != null ? header.getUserId() : "SYSTEM");

        return UpdateResponseDto.builder()
                .applicationId(app.getApplicationId())
                .customerId(app.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus(nextStage)
                .build();
    }
}
