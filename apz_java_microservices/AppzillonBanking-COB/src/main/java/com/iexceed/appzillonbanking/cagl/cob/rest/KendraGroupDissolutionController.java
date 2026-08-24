package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.KendraGroupDissolveRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.KendraGroupDissolveRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.KendraGroupDissolutionService;
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
import reactor.core.publisher.Mono;


@RestController
@Tag(description = "application/cob", name = "Customer Onboarding")
@RequestMapping("application/cob")
public class KendraGroupDissolutionController {
    private static final Logger logger = LogManager.getLogger(KendraGroupDissolutionController.class);

    @Autowired
    private KendraGroupDissolutionService kendraGroupDissolutionService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "kendra-group dissolved successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Kendra-Group Dissolve", description = "Scheduler/IT triggers dissolution of Kendra(s)/Group(s) after 90-day inactivity, or manually.")
    @PutMapping(value = "/kendra-group/dissolve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> kendraGroupDissolve(
            @RequestBody KendraGroupDissolveRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received Kendra-Group Dissolve Request.");
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        KendraGroupDissolveRequest request = requestWrapper.getApiRequest();
        Mono<Response> responseMono = kendraGroupDissolutionService.dissolveKendraGroup(request, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("Kendra-Group Dissolve Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }
}