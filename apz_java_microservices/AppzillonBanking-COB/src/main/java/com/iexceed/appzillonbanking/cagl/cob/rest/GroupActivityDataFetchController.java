package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.GroupActivityFetchRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.CGTService;
import com.iexceed.appzillonbanking.cagl.cob.service.GRTService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import reactor.core.publisher.Mono;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/cob")
public class GroupActivityDataFetchController {

    private static final Logger logger = LogManager.getLogger(GroupActivityDataFetchController.class);

    @Autowired
    private CGTService cgtService;

    @Autowired
    private GRTService grtService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group Activity details fetched successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Fetch CGT/GRT details of the particular group.", description = "Fetch the recorded CGT/GRT details based on groupId")
    @PostMapping(value = "/groupActivity/fetchDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> fetchDetails(
            @RequestBody GroupActivityFetchRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        String stage = requestWrapper.getApiRequest().getRequestObj().getStage();
        String groupId = requestWrapper.getApiRequest().getRequestObj().getGroupId();

        logger.info("Received Group Activity Fetch Details Request for Group Id :: {}, Stage :: {}", groupId, stage);
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug(" Header ::  {}", header);
        Mono<Response> responseMono;
        if(stage.equalsIgnoreCase("CGT")){
            responseMono = cgtService.fetchCgtDayDetails(groupId, header);
        } else if (stage.equalsIgnoreCase("GRT")) {
            responseMono = grtService.fetchGRTDetails(groupId, header);
        } else {
            logger.error("Invalid stage received : {}. Expected CGT or GRT.", stage);
            ResponseHeader responseHeader = new ResponseHeader();
            com.iexceed.appzillonbanking.core.payload.ResponseBody responseBody = new ResponseBody();
            responseBody.setResponseObj("Invalid stage : " + stage + ". Expected CGT or GRT.");
            CommonUtils.generateHeaderForFailure(responseHeader, "Invalid stage. Expected CGT or GRT.");
            responseMono = Mono.just(Response.builder()
                    .responseHeader(responseHeader)
                    .responseBody(responseBody)
                    .build());
        }
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("Group Activity fetch details Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }
}
