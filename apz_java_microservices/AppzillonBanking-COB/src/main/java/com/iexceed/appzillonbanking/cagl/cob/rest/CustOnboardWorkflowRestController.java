package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.OnboardingWorkflowRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.OnboardingWorkflowRequestWrapper;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.iexceed.appzillonbanking.cagl.cob.service.CustOnboardWorkflowProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Onboarding-microservice-local REST entry point. Unchanged from the
 * original version — it never touches TbObWorkflowDefinition/
 * TbObWorkflowMaster fields directly, so the switch to the real tables
 * doesn't affect anything here.
 *
 * Expected request body (flat JSON):
 *   { "appId": "APZCBO", "workflowId": "OBKMINPUT", "currentStage": "BRESUCCESS",
 *     "action": "SUBMIT", "seqNo": "", "applicationId": "12345" }
 * Leave "seqNo" blank/absent for the onboarding flow as designed.
 */
    @Validated
@RestController
@Tag(description = "application/cob", name = "Onboarding")
@RequestMapping("application/cob")
@RequiredArgsConstructor
public class CustOnboardWorkflowRestController {


    @Autowired
    private final CustOnboardWorkflowProcess custOnboardWorkflowProcess;

    private static final Logger logger = LogManager.getLogger(CustOnboardWorkflowRestController.class);

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Process Onboarding Workflow Transition",
            description = "API for resolving the next stage/role for a customer onboarding workflow action")
    @PostMapping(value = "/onboardingWorkflow/process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> processWorkflow(@RequestBody OnboardingWorkflowRequestWrapper requestWrapper,
                                                           @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
                                                           @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.info("Start : processWorkflow :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);

        OnboardingWorkflowRequest request = requestWrapper.getApiRequest();
        Response response = custOnboardWorkflowProcess.process(request, header);

        responseWrapper.setApiResponse(response);
        logger.info("End : processWorkflow :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
