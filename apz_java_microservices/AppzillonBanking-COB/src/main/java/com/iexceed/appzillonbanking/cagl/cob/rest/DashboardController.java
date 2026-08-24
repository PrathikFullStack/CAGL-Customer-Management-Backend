package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.DashboardService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/cob/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Get Dashboard Counts", description = "API for fetching counts for all dashboard tiles")
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> getDashboardCounts(
            @RequestBody DashboardRequestWrapper requestWrapper,
            @RequestHeader(required = false) String appId,
            @RequestHeader(required = false) String interfaceId,
            @RequestHeader(required = false) String userId,
            @RequestHeader(required = false) String masterTxnRefNo,
            @RequestHeader(required = false) String deviceId) {

        log.info("Start : getDashboardCounts :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        log.debug("Header :: {}", header);
        DashboardRequest request = requestWrapper.getApiRequest();
        Response response = dashboardService.getDashboardCounts(request,header);
        responseWrapper.setApiResponse(response);
        log.info("End : getDashboardCounts :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Get Dashboard List", description = "API for fetching a paginated list for a specific dashboard tile")
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> getDashboardList(
            @RequestBody DashboardListRequestWrapper requestWrapper,
            @RequestHeader(required = false) String appId,
            @RequestHeader(required = false) String interfaceId,
            @RequestHeader(required = false) String userId,
            @RequestHeader(required = false) String masterTxnRefNo,
            @RequestHeader(required = false) String deviceId) {

        log.info("Start : getDashboardList :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        log.debug("Header :: {}", header);
        DashboardListRequest request = requestWrapper.getApiRequest();
        ResponseWrapper response =dashboardService.getDashboardList(request, header);
        log.info("End : getDashboardList :: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    }

