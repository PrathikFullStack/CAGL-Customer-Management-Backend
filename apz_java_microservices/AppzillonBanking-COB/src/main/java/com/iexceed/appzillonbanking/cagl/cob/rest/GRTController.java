package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.GRTSubmitRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.GRTSubmitRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.GRTService;
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
import reactor.core.publisher.Mono;

@Slf4j
@Validated
@RestController
@Tag(description = "application/cob", name = "Onboarding")
@RequestMapping("application/cob")
@RequiredArgsConstructor
public class GRTController {

    private static final Logger logger = LogManager.getLogger(GRTController.class);

    @Autowired
    private GRTService grtService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GRT details submitted successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Submit GRT", description = "AM submits GRT details.")
    @PostMapping(value = "/grt/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> submitBMReInterview(
            @RequestBody GRTSubmitRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received GRT Request.");
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        GRTSubmitRequest request = requestWrapper.getApiRequest();
        Mono<Response> responseMono = grtService.submitGRT(request, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("GRT submit Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }

//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "GRT details fetched successfully"),
//            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
//            @ApiResponse(responseCode = "404", description = "Resource Not Found")
//    })
//    @Operation(summary = "Fetch GRT details of the particular group.", description = "Fetch the recorded GRT details based on groupId")
//    @GetMapping(value = "/grt/fetchDetails", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ResponseWrapper>> fetchGRTDetails(
//            @RequestParam String groupId,
//            @RequestHeader String appId,
//            @RequestHeader String interfaceId,
//            @RequestHeader String userId,
//            @RequestHeader String masterTxnRefNo,
//            @RequestHeader String deviceId) {
//
//        logger.info("Received GRT Fetch Details Request for Group Id :: {}", groupId);
//        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
//        logger.debug(" Header ::  {}", header);
//        Mono<Response> responseMono = grtService.fetchGRTDetails(groupId, header);
//        return responseMono.map(response -> {
//            ResponseWrapper responseWrapper = new ResponseWrapper();
//            responseWrapper.setApiResponse(response);
//            logger.debug("GRT fetch details Response :: {}", response);
//            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
//        });
//    }
}
