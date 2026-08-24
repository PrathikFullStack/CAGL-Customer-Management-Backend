package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.service.LeadService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@Tag(description = "application/cob", name = "Onboarding")
@RequestMapping("application/cob")
@RequiredArgsConstructor
public class LeadController {

    @Autowired
    private final LeadService leadService;

    private static final Logger logger = LogManager.getLogger(LeadController.class);

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Create Lead", description = "API for creating Lead(s)")
    @PostMapping(value = "/lead/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> createLeads(@RequestBody CreateLeadRequestWrapper requestWrapper, @RequestHeader String appId,
            @RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Start : createLeads :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        CreateLeadRequest request = requestWrapper.getApiRequest();
        Response response = leadService.createLeads(request, header);
        responseWrapper.setApiResponse(response);
        logger.info("End : createLeads :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Reject Lead", description = "API for rejecting a lead in COB, forwarded to CAGL")
    @PostMapping(value = "/lead/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> rejectLead(@RequestBody RejectLeadRequestWrapper requestWrapper,
                                                      @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
                                                      @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.info("Start : rejectLead :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);

        RejectLeadRequest request = requestWrapper.getApiRequest();
        Response response = leadService.rejectLead(request, header);

        responseWrapper.setApiResponse(response);
        logger.info("End : rejectLead :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
