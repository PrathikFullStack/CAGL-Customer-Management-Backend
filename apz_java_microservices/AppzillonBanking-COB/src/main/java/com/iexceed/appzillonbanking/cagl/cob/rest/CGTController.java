package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.CGTDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.CGTDetailsRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.CGTService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class CGTController {
    private static final Logger logger = LogManager.getLogger(CGTController.class);

    @Autowired
    private CGTService cgtService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CGT Conducted Successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "CGT Conduct", description = "CGT should be conduct upto mandatory days by km")
    @PostMapping(value = "/cgt/schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> conductCGT(
            @RequestBody CGTDetailsRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received CGT Conduct Request :: {}", requestWrapper);
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        CGTDetailsRequest apiRequest = requestWrapper.getApiRequest();
        Mono<Response> responseMono = cgtService.conductCGT(apiRequest, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("End : CGT Conduct response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }
    
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "CGT day details fetched successfully"),
//            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
//            @ApiResponse(responseCode = "404", description = "Resource Not Found")
//    })
//    @Operation(summary = "Fetch each day details of the particular group.", description = "Fetch the each day details based on groupId")
//    @GetMapping(value = "/cgt/fetchDayDetails", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ResponseWrapper>> fetchCGTDayDetails(
//            @RequestParam String groupId,
//            @RequestHeader String appId,
//            @RequestHeader String interfaceId,
//            @RequestHeader String userId,
//            @RequestHeader String masterTxnRefNo,
//            @RequestHeader String deviceId) {
//
//        logger.info("Received CGT Day Details Request for Group Id :: {}", groupId);
//        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
//        logger.debug("Header :: {}", header);
//        Mono<Response> responseMono = cgtService.fetchCgtDayDetails(groupId, header);
//        return responseMono.map(response -> {
//            ResponseWrapper responseWrapper = new ResponseWrapper();
//            responseWrapper.setApiResponse(response);
//            logger.debug("End : CGT fetch day details response :: {}", response);
//            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
//        });
//    }
}
