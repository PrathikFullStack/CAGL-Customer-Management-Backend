package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationService;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Validated
@RestController
@Tag(description = "application/cob", name = "Onboarding")
@RequestMapping("application/cob")
@RequiredArgsConstructor
public class ApplicationController {

    @Autowired
    private final ApplicationService applicationService;

    @Value("${onboarding.record-lock.timed-lock-duration-minutes:30}")
    private long lockDurationMinutes;

    private static final Logger logger = LogManager.getLogger(ApplicationController.class);

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Create Application", description = "API for creating application")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> createApplication(@RequestBody CreateApplicationRequestWrapper requestWrapper
//                                                            , @RequestHeader String appId,
//                                                             @RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
//                                                             @RequestHeader String deviceId
                                                            ) throws JsonProcessingException {

        logger.info("Start : createApplication :: {}", requestWrapper);
//        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
//        logger.debug("Header :: {}", header);
        CreateApplicationRequest request = requestWrapper.getApiRequest();
        ResponseWrapper responseWrapper = applicationService.createApplication(request);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Fetch Application Details", description = "API for fetching application details")
    @PostMapping("/details")
    public Mono<ResponseWrapper> getApplicationDetails(
            @RequestBody FetchApplicationDetailsRequestWrapper requestWrapper) throws NoSuchFieldException, IllegalAccessException {

        FetchApplicationDetailsRequest request = requestWrapper.getApiRequest();

        return Mono.fromCallable(() -> applicationService.getApplicationDetails(request, lockDurationMinutes))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Update Application", description = "API for updating application for each stage")
    @PutMapping("/update")
    public ResponseEntity<ResponseWrapper> updateApplication(@Valid @RequestBody UpdateApplicationRequestWrapper requestWrapper) throws JsonProcessingException {
        UpdateApplicationRequest request = requestWrapper.getApiRequest();
        log.info("PUT Starts:: /application/update request:{}", request);

        ResponseWrapper response = applicationService.updateApplication(request);
        return ResponseEntity.ok(response);
    }
}
