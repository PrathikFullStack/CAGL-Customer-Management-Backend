package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.databind.ObjectMapper;import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;import com.iexceed.appzillonbanking.cagl.cob.payload.OnboardingWorkflowRequest;import com.iexceed.appzillonbanking.cagl.cob.payload.OnboardingWorkflowResponse;import com.iexceed.appzillonbanking.cagl.cob.payload.WorkflowRequestFields;import com.iexceed.appzillonbanking.core.payload.Header;import com.iexceed.appzillonbanking.core.payload.Response;import com.iexceed.appzillonbanking.core.payload.ResponseBody;import com.iexceed.appzillonbanking.core.payload.ResponseHeader;import com.iexceed.appzillonbanking.core.utils.CommonUtils;import jakarta.transaction.Transactional;import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowDefinition;

/**
 * Onboarding-microservice-local equivalent of WorkflowProcess, now driven by
 * TbObWorkflowDefinition (real table: tb_ob_workflow_definition).
 *
 * Getter change vs. the old entity: definition.getRule() -> getRuleId(),
 * since the real entity's rule field is named ruleId (column rule_id).
 *
 * Still NOT wired to a rule engine — see original note: the rule-linked
 * branch below just logs loudly if RULE_ID is ever populated, since every
 * row in the seed data so far has a blank rule_id and there's no local
 * rule-engine dependency wired into this microservice.
 */
@Service
public class CustOnboardWorkflowProcess {

    private static final Logger logger = LogManager.getLogger(CustOnboardWorkflowProcess.class);

    @Autowired
    private CustOnboardWorkflowService custOnboardWorkflowService;

    @Autowired
    private CustOnboardApplnWorkflowService custOnboardApplnWorkflowService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Response process(OnboardingWorkflowRequest request, Header header) {

        logger.info("Inside process");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            String appId = request.getAppId();
            String userId = request.getUserId();
            String presentRole = request.getPresentRole();
            WorkflowRequestFields requestObj = request.getRequestObj();
            String workflowId = requestObj.getWorkflowId();
            String currentStage = requestObj.getCurrentStage();
            String action = requestObj.getAction();
            String applicationId = requestObj.getApplicationId();

            logger.debug("CustOnboardWorkflowProcess.process - appId:{}, workflowId:{}, currentStage:{}, action:{}, applicationId:{}",
                    appId, workflowId, currentStage, action, applicationId);

            TbObWorkflowDefinition definition = custOnboardWorkflowService.getWorkflowDefinition(appId, workflowId,
                    currentStage, action);

            OnboardingWorkflowResponse workflowResponse = new OnboardingWorkflowResponse();

            if (null != definition && null != definition.getWorkflowId() && !definition.getWorkflowId().isEmpty()) {

                if (null != definition.getRuleId() && !definition.getRuleId().trim().isEmpty()) {
                    logger.error("Definition row for workflowId:{}, fromStageId:{}, action:{} is rule-linked "
                                    + "(RULE_ID={}), but this onboarding microservice has no local rule engine wired up yet. "
                                    + "Falling back to this row's static next_role/next_stage_id/next_workflow_status.",
                            workflowId, currentStage, action, definition.getRuleId());
                }

                // Persist the transition against this specific application —
                TbObApplnWorkflow applnWorkflow = custOnboardApplnWorkflowService.recordTransition(appId,
                        applicationId, userId, definition, presentRole);
                logger.debug("Persisted appln workflow row :: {}", applnWorkflow);

                workflowResponse.setStatus("SUCCESS");
                workflowResponse.setNextRole(definition.getNextRole());
                workflowResponse.setNextStageId(definition.getNextStageId());
                workflowResponse.setNextWorkflowStatus(definition.getNextWorkflowStatus());
                workflowResponse.setWorkflowId(definition.getWorkflowId());

                responseBody.setResponseObj(objectMapper.writeValueAsString(workflowResponse));
                CommonUtils.generateHeaderForSuccess(responseHeader);
            } else {
                logger.warn("No workflow definition found for appId:{}, workflowId:{}, fromStageId:{}, action:{}",
                        appId, workflowId, currentStage, action);
                workflowResponse.setStatus("FAILURE");
                workflowResponse.setErrorCode("DATA_ERROR");
                workflowResponse.setErrorMessage("No workflow definition record found");

                responseBody.setResponseObj(objectMapper.writeValueAsString(workflowResponse));
                CommonUtils.generateHeaderForFailure(responseHeader, "No workflow definition record found");
            }
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        } catch (Exception ex) {
            logger.error("Exception while processing onboarding workflow", ex);
            responseBody.setResponseObj("Unable to process onboarding workflow.");
            CommonUtils.generateHeaderForFailure(responseHeader, "Exception occurred while processing onboarding workflow");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        return response;
    }
}