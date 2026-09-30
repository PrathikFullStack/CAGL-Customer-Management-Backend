package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowHistoryDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowQueueItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowTransitionRequest;
import com.iexceed.appzillonbanking.cagl.cm.service.WorkflowEngineService;

@RestController
@RequestMapping("/api/v1/cm/workflow")
public class WorkflowRestController {

    private static final Logger logger = LogManager.getLogger(WorkflowRestController.class);

    private final WorkflowEngineService workflowEngine;

    public WorkflowRestController(WorkflowEngineService workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    /**
     * Executes workflow transition (e.g. SUBMIT, APPROVED, PUSHBACK, REJECT, RESPOND, VERIFY, RETRIGGER)
     */
    @PostMapping("/transition")
    public ResponseEntity<ResponseWrapper<String>> transitionWorkflow(
            @RequestBody RequestWrapper<WorkflowTransitionRequest> request) {
        WorkflowTransitionRequest body = request.getBody();
        logger.info("Received workflow transition request for App ID: {}, Stage: {}, Action: {}",
                body.getApplicationId(), body.getCurrentStage(), body.getAction());

        String nextStage = workflowEngine.transitionWorkflow(body, request.getHeader());
        return ResponseEntity.ok(ResponseWrapper.success(nextStage, "Workflow transitioned to " + nextStage));
    }

    /**
     * Retrieves work items in a user queue (e.g., BMQUEUE, AMQUEUE, RPCMAKERQUEUE, AMLQUEUE, CRTQUEUE, etc.)
     */
    @GetMapping("/queue")
    public ResponseEntity<ResponseWrapper<List<WorkflowQueueItemDto>>> getQueueItems(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String branchId) {
        logger.info("Fetching queue tasks for Role: {}, Stage: {}, Branch: {}", role, stage, branchId);
        List<WorkflowQueueItemDto> items = workflowEngine.getQueueItems(role, stage, branchId);
        return ResponseEntity.ok(ResponseWrapper.success(items, "Queue items fetched successfully"));
    }

    /**
     * Retrieves audit trail / movement history of an application
     */
    @GetMapping("/history/{applicationId}")
    public ResponseEntity<ResponseWrapper<List<WorkflowHistoryDto>>> getApplicationHistory(
            @PathVariable String applicationId) {
        logger.info("Fetching workflow audit trail for App ID: {}", applicationId);
        List<WorkflowHistoryDto> history = workflowEngine.getApplicationWorkflowHistory(applicationId);
        return ResponseEntity.ok(ResponseWrapper.success(history, "Workflow history fetched successfully"));
    }
}
