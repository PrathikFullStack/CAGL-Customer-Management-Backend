package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.BlacklistKendraRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.BlacklistKendraRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.BlacklistKendraService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@Tag(description = "application/cob", name = "Blacklist Kendra")
@RequestMapping("application/cob")
@RequiredArgsConstructor
public class BlacklistKendraController {

    @Autowired
    private final BlacklistKendraService blacklistKendraService;

    private static final Logger logger = LogManager.getLogger(BlacklistKendraController.class);

    @ApiResponses({@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
    @Operation(summary = "Update Kendra Blacklist Status", description = "Single API for both blacklisting and unblacklisting a Kendra in COB — action=\"BLOCK\" or \"UNBLOCK\" — forwarded to CAGL")
    @PostMapping(value = "/kendra/blacklist", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> updateBlacklistStatus(@RequestBody BlacklistKendraRequestWrapper requestWrapper,
                                                                 @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
                                                                 @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.info("Start : updateBlacklistStatus :: {}", requestWrapper);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        BlacklistKendraRequest request = requestWrapper.getApiRequest();
        Response response = blacklistKendraService.updateBlacklistStatus(request, header);
        responseWrapper.setApiResponse(response);
        logger.info("End : updateBlacklistStatus :: {}", response);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
