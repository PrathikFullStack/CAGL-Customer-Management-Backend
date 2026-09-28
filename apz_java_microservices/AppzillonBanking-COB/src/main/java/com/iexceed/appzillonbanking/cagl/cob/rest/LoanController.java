package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.EditLoanDetailsRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.SaveLoanDetailsRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.LoanService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("application/cob")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    private LoanService loanService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
    })
    @Operation(summary = "Save Loan and Charge Details", description = "API to Save Loan Details along with Nominee, Insurance and Charge Breakup")
    @PostMapping(value = "/saveloandetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> saveLoanDetails(@RequestBody SaveLoanDetailsRequestWrapper requestWrapper,
                                                                 @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
                                                                 @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.debug("saveLoanDetails request data :: {}", requestWrapper);

        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("saveLoanDetails Header value :: {}", header);

        return loanService.saveLoanDetails(requestWrapper.getApiRequest(), header)
                .map(response -> {
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    responseWrapper.setApiResponse(response);
                    logger.debug("End : saveLoanDetails response :: {}", response);
                    return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
                });
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
    })
    @Operation(summary = "Edit Loan and Charge Details", description = "API to Edit existing Loan Details along with Nominee, Insurance and Charge Breakup")
    @PostMapping(value = "/editloandetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> editLoanDetails(@RequestBody EditLoanDetailsRequestWrapper requestWrapper,
                                                                 @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
                                                                 @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

        logger.debug("editLoanDetails request data :: {}", requestWrapper);

        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("editLoanDetails Header value :: {}", header);

        return loanService.editLoanDetails(requestWrapper.getApiRequest(), header)
                .map(response -> {
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    responseWrapper.setApiResponse(response);
                    logger.debug("End : editLoanDetails response :: {}", response);
                    return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
                });
    }
}