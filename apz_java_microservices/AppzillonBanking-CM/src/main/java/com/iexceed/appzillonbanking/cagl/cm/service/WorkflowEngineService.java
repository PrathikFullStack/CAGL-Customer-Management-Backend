package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowHistoryDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowQueueItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowTransitionRequest;

public interface WorkflowEngineService {

    /**
     * Executes dynamic workflow transition following the team lead matrix
     *
     * @param request Workflow transition payload
     * @param header  Request header with actor details
     * @return Resulting next stage code
     */
    String transitionWorkflow(WorkflowTransitionRequest request, RequestHeader header);

    /**
     * Fetches queue tasks pending for a specific role or stage
     *
     * @param role     Role identifier
     * @param stage    Stage code
     * @param branchId Branch identifier
     * @return List of queue items
     */
    List<WorkflowQueueItemDto> getQueueItems(String role, String stage, String branchId);

    /**
     * Fetches complete movement history for an application
     *
     * @param applicationId Application identifier
     * @return Ordered list of workflow movement audit records
     */
    List<WorkflowHistoryDto> getApplicationWorkflowHistory(String applicationId);
}
