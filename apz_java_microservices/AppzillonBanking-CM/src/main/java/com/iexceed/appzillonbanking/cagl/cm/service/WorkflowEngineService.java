package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowHistoryDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowQueueItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.workflow.WorkflowTransitionRequest;

public interface WorkflowEngineService {


    String transitionWorkflow(WorkflowTransitionRequest request, RequestHeader header);

    List<WorkflowQueueItemDto> getQueueItems(String role, String stage, String branchId);

    List<WorkflowHistoryDto> getApplicationWorkflowHistory(String applicationId);
}
