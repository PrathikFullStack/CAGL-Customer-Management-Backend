package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.service.WorkflowEngineService;

@RestController
@RequestMapping("/api/v1/cm/workflow")
public class WorkflowRestController {

    private static final Logger logger = LogManager.getLogger(WorkflowRestController.class);

    private final WorkflowEngineService workflowEngine;

    public WorkflowRestController(WorkflowEngineService workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @PostMapping("/transition")
    public ResponseEntity<ResponseWrapper<String>> transitionWorkflow(
            @RequestBody RequestWrapper<Map<String, String>> request) {
        Map<String, String> body = request.getBody();
        String appId = body.getOrDefault("appId", "APZ_CM");
        String applicationId = body.get("applicationId");
        String fromStage = body.get("fromStage");
        String action = body.get("action");
        String remarks = body.get("remarks");

        String userId = request.getHeader() != null ? request.getHeader().getUserId() : "SYSTEM";
        String userName = request.getHeader() != null ? request.getHeader().getUserId() : "SYSTEM";
        String userRole = request.getHeader() != null ? request.getHeader().getUserRole() : "BM";

        logger.info("Executing workflow transition on App ID: {}, Action: {}", applicationId, action);
        String nextStage = workflowEngine.transitionWorkflow(appId, applicationId, 1, fromStage,
                action, userId, userName, userRole, remarks);

        return ResponseEntity.ok(ResponseWrapper.success(nextStage, "Workflow transitioned successfully"));
    }
}
