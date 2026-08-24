package com.iexceed.appzillonbanking.cagl.cob.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cob.payload.BulkUploadRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.PopulateapplnWFRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationWorkflowService;
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

@Slf4j
@Validated
@RestController
@Tag(description = "application/onboarding", name = "Onboarding")
@RequestMapping("application/onboarding")
@RequiredArgsConstructor
public class ApplicationWorkflowController {

	@Autowired
	private ApplicationWorkflowService applicationWorkflowService;

	private static final Logger logger = LogManager.getLogger(ApplicationWorkflowController.class);

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Update application workflow", description = "API to populate application workflow")
	@PostMapping(value = "/updateWorkflow", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> updateWorkflow(@RequestBody PopulateapplnWFRequestWrapper request,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.info("Start : populateWorkflow :: {}", request);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header :: {}", header);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response response = applicationWorkflowService.updateStage(request.getApiRequest(), header);
		responseWrapper.setApiResponse(response);
		logger.info("End : populateWorkflow :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Save Bulk Data", description = "API to Save Bulk Excel Data into DB")
	@PostMapping(value = "/insertExcelData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> insertExcelDataIntoDB(@RequestBody BulkUploadRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String appId,
			@RequestHeader(defaultValue = "SaveExcelData") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response response = new Response();
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("SaveExcelData Header value :: {}", header);
		try {
			response = applicationWorkflowService.saveExcelData(requestWrapper.getApiRequest(),header);
			logger.debug("Final response :: {}", response);
		} catch (Exception e) {
			logger.error("Error Occured in SaveExcelData:{}", e);
		}
		responseWrapper.setApiResponse(response);
		logger.debug("End : SaveExcelData response :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

}
