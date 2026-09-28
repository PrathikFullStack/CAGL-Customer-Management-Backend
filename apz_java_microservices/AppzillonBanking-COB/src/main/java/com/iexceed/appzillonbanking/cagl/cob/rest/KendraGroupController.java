package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.service.KendraGroupService;
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
public class KendraGroupController {

    private static final Logger logger = LogManager.getLogger(KendraGroupController.class);

    @Autowired
    private KendraGroupService kendraGroupService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Create Kendra", description = "API for creating Kendra")
    @PostMapping(value = "/kendra/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> createKendra(
            @RequestBody CreateKendraRequestWrapper requestWrapper, @RequestHeader String appId, @RequestHeader String interfaceId,
            @RequestHeader String userId, @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) throws JsonProcessingException {
        logger.info("Start : createKendra :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(
                appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        CreateKendraRequest request = requestWrapper.getApiRequest();
        Response response = kendraGroupService.createKendra(request, header);
        responseWrapper.setApiResponse(response);
        logger.info("End : createKendra :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Create Group", description = "API for creating Group under a Kendra")
    @PostMapping(value = "/group/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> createGroup(@RequestBody CreateGroupRequestWrapper requestWrapper,
                                                       @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
                                                       @RequestHeader String deviceId) throws JsonProcessingException {

        logger.info("Start : createGroup :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        CreateGroupRequest request = requestWrapper.getApiRequest();
        Response response = kendraGroupService.createGroup(request, header);
        responseWrapper.setApiResponse(response);
        logger.info("End : createGroup :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @Operation(summary = "Fetch Group Member Count", description = "API for fetching group member count")
    @PostMapping(value = "/group/memberCount", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> fetchGroupMemberCount(@RequestBody FetchGroupMemberCountRequestWrapper requestWrapper,
                                                           @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
                                                           @RequestHeader String deviceId) throws JsonProcessingException {
        logger.info("Start : fetchGroupCount :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        FetchGroupMemberCountRequest request = requestWrapper.apiRequest();
        Response response = kendraGroupService.fetchGroupMemberCount(request, header);
        responseWrapper.setApiResponse(response);
        logger.info("End : fetchGroupCount :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
