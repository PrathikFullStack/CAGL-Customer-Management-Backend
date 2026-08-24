package com.iexceed.appzillonbanking.cagl.cob.rest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.SearchRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.SearchRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.SearchService;
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
public class SearchController {

    private final SearchService searchService;

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Unified Search", description = "API for performing global and filter-based searches.")
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<ResponseWrapper> search(
            @RequestBody SearchRequestWrapper requestWrapper,
            @RequestHeader(required = false) String appId,
            @RequestHeader(required = false) String interfaceId,
            @RequestHeader(required = false) String userId,
            @RequestHeader(required = false) String masterTxnRefNo,
            @RequestHeader(required = false) String deviceId) {

        log.info("Start : getDashboardList :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        log.debug("Header :: {}", header);
        SearchRequest request = requestWrapper.getApiRequest();
        ResponseWrapper response = searchService.search(request, header);
        log.info("End : search :: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
