package com.iexceed.appzillonbanking.cagl.cob.service;

import com.iexceed.appzillonbanking.cagl.cob.exception.BMReinterviewValidationException;
import com.iexceed.appzillonbanking.cagl.cob.payload.BMReInterviewRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.BMReInterviewRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.BMReinterviewCustomerDetails;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.BMReinterviewOutcome;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.BMReinterviewHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.BMReinterviewResponseMapper;

import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;

import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Facade in front of the BM Reinterview subsystem: {@link BMReinterviewHandler} (persistence,
 * running as its own transaction) and {@link BMReinterviewResponseMapper} (response shaping).
 * {@link com.iexceed.appzillonbanking.cagl.cob.rest.BMReinterviewController} only ever talks to
 * this class -- it doesn't need to know that a submission touches three tables across two
 * collaborators, or how the response payload gets assembled.
 * <p>
 * This is also the exception boundary for the whole submission: any failure raised while
 * validating the request or while {@link BMReinterviewHandler#process} runs is caught here and
 * turned into a "failure" {@link Response} (per this API's contract, failures are still a 200 with
 * a failure header rather than a non-2xx HTTP status) -- it never escapes as an unhandled
 * exception. A {@link BMReinterviewValidationException} is a deliberate, caller-facing failure
 * (bad request state, a lookup that legitimately came back empty, ...) so its message is shown
 * as-is; anything else is unexpected, and gets a generic message instead of leaking exception
 * internals (a stack-trace fragment, a raw SQL error, ...) into the API response.
 */
@Service
@RequiredArgsConstructor
public class BMReinterviewService {

    public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";

    private static final Logger logger = LogManager.getLogger(BMReinterviewService.class);

    private final BMReinterviewHandler bmReinterviewProcessor;
    private final BMReinterviewResponseMapper responseMapper;

    public Mono<Response> submitBMReInterview(BMReInterviewRequest request, Header header) {

        logger.info("BM ReInterview API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            BMReInterviewRequestFields requestObj = request.getRequestObj();
            logger.info("Processing BM ReInterview for Application Id : {}, Customer Id : {}, subStage : {}, " +
                            "subStageStatus : {}, isDraft : {}",
                    requestObj.getApplicationId(), requestObj.getCustomerId(), requestObj.getSubStage(),
                    requestObj.getSubStageStatus(), requestObj.getIsDraft());

            BMReinterviewCustomerDetails custDetails = requestObj.getCustDetails() != null
                    ? requestObj.getCustDetails() : new BMReinterviewCustomerDetails();

            logger.debug("Decision on this call : {}", custDetails.getDecision());

            validateRequest(requestObj, custDetails);
            logger.debug("Request validation passed for Application Id : {}", requestObj.getApplicationId());

            logger.info("Delegating to BMReinterviewProcessor for Application Id : {}", requestObj.getApplicationId());

            BMReinterviewOutcome outcome = bmReinterviewProcessor.process(
                    requestObj,
                    custDetails,
                    request.getAppId(),
                    request.getUserId(),
                    request.getUserName(),
                    request.getUserRole(),
                    request.getVersionNum(),
                    request.getInterfaceName()
            );
            logger.info("BMReinterviewProcessor returned for Application Id : {} -- revertedToCgt : {}",
                    requestObj.getApplicationId(), outcome.revertedToCgt());

            if (outcome.revertedToCgt()) {
                logger.info("Group Id : {} reverted to CGT -- skipping the normal response mapping.",
                        requestObj.getGroupId());
                responseBody.setResponseObj("Group-Id: " + requestObj.getGroupId()
                        + " successfully revert back to CGT stage");
            } else {
                logger.debug("Building BM ReInterview response JSON for Application Id : {}", requestObj.getApplicationId());
                responseBody.setResponseObj(
                        responseMapper.toResponseJson(outcome.reinterview(), requestObj, outcome.kycDetails(),
                                outcome.locationDetails(), outcome.loanDetails()));
            }
            CommonUtils.generateHeaderForSuccess(responseHeader);
            responseHeader.setResponseMessage("BM ReInterview submitted successfully");
            logger.info("BM ReInterview API completed successfully for Application Id : {}", requestObj.getApplicationId());

        } catch (BMReinterviewValidationException ex) {
            logger.error("Validation failed for BM ReInterview request : {}", ex.getMessage(), ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, "Failed to submit the application data. Reason: " + ex.getMessage());
            responseHeader.setResponseCode("1");
        } catch (Exception ex) {
            logger.error("Exception while saving BM ReInterview.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_MSG);
            responseHeader.setResponseCode("2");
        }
        return Mono.just(Response.builder().responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /** A draft can never carry a decision -- a decision only makes sense on a real submit. */
    private void validateRequest(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails) {
        if (Boolean.TRUE.equals(requestObj.getIsDraft()) && isDecisionPresent(custDetails.getDecision())) {
            throw new BMReinterviewValidationException("isDraft cannot be true when a decision is provided.");
        }
    }

    private boolean isDecisionPresent(String decision) {
        return decision != null && !decision.isBlank();
    }
}