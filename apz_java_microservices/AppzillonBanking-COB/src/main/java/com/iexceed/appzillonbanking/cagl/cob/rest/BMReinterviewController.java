package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.BMReInterviewRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.BMReInterviewRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.BMReinterviewService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(description = "application/cob", name = "Customer Onboarding")
@RequestMapping("application/cob")
public class BMReinterviewController {

    private static final Logger logger = LogManager.getLogger(BMReinterviewController.class);

    @Autowired
    private BMReinterviewService bmReinterviewService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "BM Reinterview submitted successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Submit BM Reinterview", description = "BM submits reinterview details for member.")
    @PostMapping(value = "/bmreinterview/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> submitBMReInterview(
            @RequestBody BMReInterviewRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received BM Reinterview Request.");
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        BMReInterviewRequest request = requestWrapper.getApiRequest();
        Mono<Response> responseMono = bmReinterviewService.submitBMReInterview(request, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("BM ReInterview Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });

        // fetch API -->> TODO
    }
}
