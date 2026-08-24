package com.iexceed.appzillonbanking.cbs.rest;

import com.iexceed.appzillonbanking.cbs.payload.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iexceed.appzillonbanking.cbs.service.CBSService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@Tag(description = "application/cbs", name = "CBS")
@RequestMapping("application/cbs/")
public class CBSServiceController {

	private static final Logger logger = LogManager.getLogger(CBSServiceController.class);

	@Autowired
	CBSService cbsService;

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Pre Closure Loan Api", description = "API to perform CBS PreClose Loan")
	@PostMapping(value = "preClosureLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> preClosureLoan(
			@RequestBody PreClosureLoanRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("preclosureloan request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("preclosureloan Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.callPreClosureLoanApi(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("preclosureloan Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Penny Check Api", description = "API to perform Penny Check")
	@PostMapping(value = "pennyCheck", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> pennyCheck(@RequestBody PennyCheckRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.debug("pennyCheck request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("pennyCheck Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.callPennyCheckApi(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("pennyCheck Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Emergency Loan Creation Api", description = "API to create Emergency Loan")
	@PostMapping(value = "emergencyLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> emergencyLoan(@RequestBody EmergencyLoanRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.debug("emergencyLoan request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("emergencyLoan Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.createEmergencyLoan(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("emergencyLoan Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch Customer Loan Api", description = "API to fetch Customer Loan Detail")
	@PostMapping(value = "fetchCustomerLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> fetchCustomerLoan(
			@RequestBody FetchCustomerLoanRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("fetchCustomerLoan request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("fetchCustomerLoan Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.fetchCustomerLoan(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("fetchCustomerLoan Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch Customer PayOff Api", description = "API to fetch Customer PayOff Detail")
	@PostMapping(value = "fetchCustomerPayOffDetail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> fetchCustomerPayOffDetail(
			@RequestBody FetchCustomerPayOffDetailRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("fetchCustomerPayOffDetail request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("fetchCustomerPayOffDetail Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService
					.fetchCustomerPayOffDetail(requestWrapper.getApiRequest(), header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("fetchCustomerPayOffDetail Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Loan Increment Api", description = "API to Increment Loan")
	@PostMapping(value = "incrementLoan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> incrementLoan(@RequestBody EmergencyLoanRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.debug("incrementLoan request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("incrementLoan Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.incrementLoan(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("incrementLoan Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Earning Member Update", description = "API to Update Member Earning details")
	@PostMapping(value = "earningMemberUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> earningMemberUpdate(
			@RequestBody EarningMemberUpdateRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("earningMemberUpdate request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("earningMemberUpdate Header value :: {}", header);

		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.earningMemberUpdate(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("earningMemberUpdate Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = " Update Customer GEO", description = "API to Update Customer GEO Location details")
	@PostMapping(value = "customerGeoUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> customerGeoUpdate(
			@RequestBody UpdateCustomerGeoLocationRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("customerGeoUpdate request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("customerGeoUpdate Header value :: {}", header);

		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.customerGeoUpdate(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("customerGeoUpdate Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Kendra Geo Location Update", description = "API to Update Kendra GEO Location details")
	@PostMapping(value = "kendraGeoUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> kendraGeoUpdate(
			@RequestBody UpdateKendraGeoLocationRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("kendraGeoUpdate request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("kendraGeoUpdate Header value :: {}", header);

		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.kendraGeoUpdate(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("kendraGeoUpdate Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Income Assessment Update", description = "API to Update Income Assessment details")
	@PostMapping(value = "updateIncomeAssessment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> updateIncomeAssessment(
			@RequestBody UpdateIncomeAssessmentRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.debug("updateIncomeAssessment request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("updateIncomeAssessment Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService
					.updateIncomeAssessment(requestWrapper.getApiRequest(), header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("updateIncomeAssessment Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	// this is for disbursementStatus API Start

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Disbursement Status", description = "Disbursement Status API")
	@PostMapping(value = "disbursementStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> callDisbursedStatus(
			@RequestBody PreClosureLoanRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {
		logger.debug("Disbursement Status request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Disbursement Status Header value :: {}", header);
		// String loanRefId =
		// requestWrapper.getApiRequest().getRequestObj().getLoanRefID();
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService
					.callDisbursementStatusApi(requestWrapper.getApiRequest(), header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("Disbursement Status Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Member Activation", description = "T24 Member Activation API")
	@PostMapping(value = "addEditMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> addEditMember(@RequestBody AddEditMembersRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.debug("Add edit member request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Add edit member Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.addEditMember(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("Add edit member Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Kendra Activation", description = "T24 Kendra Activation API")
	@PostMapping(value = "addEditKendra", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> addEditKendra(@RequestBody AddEditKendraRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.debug("Add edit kendra request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Add edit Kendra Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.addEditKendra(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("Kendra creation Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Group created successfully via CAGL API"),
			@ApiResponse(responseCode = "400", description = "Invalid request parameters"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@Operation(summary = "Create Group", description = "T24 Group Activation API")
	@PostMapping(value = "addEditgroup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> createGroup(@RequestBody CreateGroupRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header value :: {}", header);

		return cbsService.createGroup(requestWrapper, header).map(responseWrapper -> {
			return ResponseEntity.ok(responseWrapper);
		}).onErrorResume(ex -> {
			logger.error("Error while processing create group request: {}", ex.getMessage(), ex);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(buildErrorResponseWrapper("Internal Server Error: " + ex.getMessage())));
		});
	}

	private ResponseWrapper buildErrorResponseWrapper(String errorMessage) {
		Response response = new Response();
		ResponseHeader respHeader = new ResponseHeader();
		ResponseBody respBody = new ResponseBody();

		respBody.setResponseObj(errorMessage);
		CommonUtils.generateHeaderForFailure(respHeader, errorMessage);
		response.setResponseBody(respBody);
		response.setResponseHeader(respHeader);

		ResponseWrapper resWrapper = new ResponseWrapper();
		resWrapper.setApiResponse(response);

		logger.error("Error response built: {}", errorMessage);
		return resWrapper;
	}
	
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
	    @ApiResponse(responseCode = "408", description = "Service Timed Out"),
	    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
	    @ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
	})
	@Operation(summary = "AML Check", description = "AML Check API")
	@PostMapping(value = "amlCheck", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> amlCheck(
	        @RequestBody AmlCheckRequestWrapper requestWrapper,
	        @RequestHeader String appId,
	        @RequestHeader String interfaceId,
	        @RequestHeader String userId,
	        @RequestHeader String masterTxnRefNo,
	        @RequestHeader String deviceId) {

	    logger.debug("AML Check request data :: {}", requestWrapper);
	    Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
	    logger.debug("AML Check Header value :: {}", header);

	    try {
	        Mono<ResponseWrapper> monoResponseWrapper = cbsService
	                .callAmlCheckApi(requestWrapper.getApiRequest(), header);
	        return monoResponseWrapper.flatMap(resWrapper -> {
	            logger.warn("AML Check Response Wrapper ==> {}", resWrapper);
	            return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
	        });
	    } catch (Exception e) {
	        ResponseWrapper errorResponseWrapper = new ResponseWrapper();
	        Response errorResponse = new Response();
	        ResponseHeader errorHeader = new ResponseHeader();
	        ResponseBody errorBody = new ResponseBody();
	        CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
	        errorBody.setResponseObj("");
	        errorResponse.setResponseBody(errorBody);
	        errorResponse.setResponseHeader(errorHeader);
	        errorResponseWrapper.setApiResponse(errorResponse);
	        return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
	    }
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Check IFSC code", description = "To check Bank IFSC code.")
	@PostMapping(value = "verifyIfsc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> verifyIFSC(@RequestBody VerifyIfscCodeRequestWrapper requestWrapper,
															   @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
															   @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.debug("Verify IFSC code request data :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Verify IFSC code Header value :: {}", header);
		try {
			Mono<ResponseWrapper> monoResponseWrapper = cbsService.verifyIFSC(requestWrapper.getApiRequest(),
					header);
			return monoResponseWrapper.flatMap(resWrapper -> {
				logger.warn("Verify IFSC code Response Wrapper ==> {}", resWrapper);
				return Mono.just(new ResponseEntity<>(resWrapper, HttpStatus.OK));
			});
		} catch (Exception e) {
			ResponseWrapper errorResponseWrapper = new ResponseWrapper();
			Response errorResponse = new Response();
			ResponseHeader errorHeader = new ResponseHeader();
			ResponseBody errorBody = new ResponseBody();
			CommonUtils.generateHeaderForFailure(errorHeader, e.getMessage());
			errorBody.setResponseObj("");
			errorResponse.setResponseBody(errorBody);
			errorResponse.setResponseHeader(errorHeader);
			errorResponseWrapper.setApiResponse(errorResponse);
			return Mono.just(new ResponseEntity<>(errorResponseWrapper, HttpStatus.OK));
		}
	}
}
