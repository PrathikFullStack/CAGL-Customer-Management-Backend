package com.iexceed.appzillonbanking.cagl.rest;

import com.iexceed.appzillonbanking.cagl.payload.CreateLeadRequest;
import com.iexceed.appzillonbanking.cagl.payload.CreateLeadRequestWrapper;
import com.iexceed.appzillonbanking.cagl.service.OnboardingService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(description = "application/cob", name = "Onboarding")
@RequestMapping("application/cob")
public class OnboardingAPI {

    @Autowired
    private OnboardingService onboardingService;

    private static final Logger logger = LogManager.getLogger(OnboardingAPI.class);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
    })
    @Operation(summary = "Create Lead", description = "API to create Lead in CDH MySQL database")
    @PostMapping(value = "/createLead", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> createLead(
            @RequestBody CreateLeadRequest requestWrapper,
            @RequestHeader String appId, @RequestHeader String interfaceId,
            @RequestHeader String userId, @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.debug("createLead request :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("createLead Header :: {}", header);
        Response response = onboardingService.createLead(requestWrapper, header);
        responseWrapper.setApiResponse(response);
        logger.debug("createLead response :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
