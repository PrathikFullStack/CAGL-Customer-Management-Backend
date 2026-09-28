package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowTransitionRequest;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.RecordLockService;
import com.iexceed.appzillonbanking.cagl.cm.service.WorkflowEngineService;

@Component
public class SubmitWorkflowUpdateHandler implements UpdateHandler {

    private final CmApplicationMasterRepository appRepo;
    private final WorkflowEngineService workflowEngine;
    private final RecordLockService lockService;

    public SubmitWorkflowUpdateHandler(
            CmApplicationMasterRepository appRepo,
            WorkflowEngineService workflowEngine,
            RecordLockService lockService) {
        this.appRepo = appRepo;
        this.workflowEngine = workflowEngine;
        this.lockService = lockService;
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
                    .remarks("Application not found")
                    .build();
        }

        CmApplicationMasterEntity app = appOpt.get();
        String currentStage = app.getStage() != null ? app.getStage() : "DRAFT";

        boolean isKycEdited = request.getUpdatePayload() != null &&
                (request.getUpdatePayload().containsKey("kycType") ||
                 request.getUpdatePayload().containsKey("kycDocId") ||
                 request.getUpdatePayload().containsKey("primaryKycId") ||
                 request.getUpdatePayload().containsKey("primaryKycType") ||
                 request.getUpdatePayload().containsKey("dob"));

        WorkflowTransitionRequest transitionReq = WorkflowTransitionRequest.builder()
                .appId("APZCBO")
                .applicationId(app.getApplicationId())
                .customerId(app.getCustomerId())
                .currentStage(currentStage)
                .action("SUBMIT")
                .initiatorRole(header != null ? header.getUserRole() : "KM")
                .isKycEdited(isKycEdited)
                .remarks(request.getRemarks())
                .build();

        String nextStage = workflowEngine.transitionWorkflow(transitionReq, header);

        // Release lock
        lockService.releaseLock(app.getApplicationId(), header != null ? header.getUserId() : "SYSTEM");

        return UpdateResponseDto.builder()
                .applicationId(app.getApplicationId())
                .customerId(app.getCustomerId())
                .section(getSectionName())
                .status("SUCCESS")
                .workflowStatus(nextStage)
                .remarks("Request submitted successfully to stage: " + nextStage)
                .build();
    }
}
