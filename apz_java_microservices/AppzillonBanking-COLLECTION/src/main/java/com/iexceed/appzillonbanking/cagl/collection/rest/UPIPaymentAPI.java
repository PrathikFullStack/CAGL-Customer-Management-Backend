package com.iexceed.appzillonbanking.cagl.collection.rest;

import com.iexceed.appzillonbanking.cagl.collection.payload.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iexceed.appzillonbanking.cagl.collection.service.UPIPaymentService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@Tag(description = "application/collection", name = "UPI Payments")
@RequestMapping("application/collection")
public class UPIPaymentAPI {

	private static final Logger logger = LogManager.getLogger(UPIPaymentAPI.class);

	@Autowired
	private UPIPaymentService upiPaymentService;

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "QR UPI Payment", description = "QR UPI Payment")
	@PostMapping(value = "/qrUPIPayment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> qrUPIPayment(@RequestBody QRUPIPaymentRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.warn("Start : qrUPIPayment with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("qrUPIPayment Header value :: {} ", header);

		QRUPIPaymentRequest qrupiPaymentRequest = requestWrapper.getApiRequest();
		Mono<Response> responseMono = upiPaymentService.qrUPIPayment(qrupiPaymentRequest, header);
		return responseMono.flatMap(response -> {
			ResponseWrapper responseWrapper = new ResponseWrapper();
			responseWrapper.setApiResponse(response);
			logger.debug("End : qrUPIPayment response :: {}", response);
			return Mono.just(new ResponseEntity<>(responseWrapper, HttpStatus.OK));
		});
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "QR UPI Payment Refresh API", description = "QR UPI Payment Refresh API")
	@PostMapping(value = "/refreshQRUPIPayment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> refreshQRUPIPayment(@RequestBody QRUPIPaymentRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.warn("Start : refreshQRUPIPayment with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("refreshQRUPIPayment Header value :: {} ", header);

		QRUPIPaymentRequest qrupiPaymentRequest = requestWrapper.getApiRequest();
		Response response = upiPaymentService.refreshQRUPIPayment(qrupiPaymentRequest, header);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		logger.debug("End : refreshQRUPIPayment response :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}
	
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "QR UPI Payment Refresh API", description = "QR UPI Payment Refresh API")
	@PostMapping(value = "/verifyQRPayStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> verifyQRPayStatus(@RequestBody VerifyQRPaymentRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {

		logger.warn("Start : verifyQRPayStatus with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("verifyQRPayStatus Header value :: {} ", header);

		VerifyQRPaymentRequest qrupiPaymentRequest = requestWrapper.getApiRequest();
		Response response = upiPaymentService.verifyQRPaymentStatus(qrupiPaymentRequest, header);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		logger.debug("End : verifyQRPayStatus response :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "QR UPI Payment Status Update", description = "QR UPI Payment Status Update")
	@PostMapping(value = "/updateQRPaymentStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> updateQRUPIPaymentStatus(
			@RequestBody QRUPIPaymentRequestWrapper requestWrapper, @RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.warn("Start : updateQRPaymentStatus with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("updateQRPaymentStatus Header value :: {} ", header);

		QRUPIPaymentRequest qrupiPaymentRequest = requestWrapper.getApiRequest();
		Response response = upiPaymentService.updateQRUPIPaymentStatus(qrupiPaymentRequest, header);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		logger.debug("End : updateQRPaymentStatus response :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Bulk transactions status check", description = "Status check for bulk transactions")
	@PostMapping(value = "/bulkStatusCheck", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> bulkStatusCheck(@RequestBody BulkStatusRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId,
			@RequestHeader String userId, @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.warn("Start : bulkStatusCheck with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("bulkStatusCheck Header value :: {}", header);

		BulkStatusRequest bulkStatusRequest = requestWrapper.getApiRequest();

		Mono<Response> responseMono = upiPaymentService.bulkStatusCheck(bulkStatusRequest, header);
		return responseMono.flatMap(response -> {
			ResponseWrapper responseWrapper = new ResponseWrapper();
			responseWrapper.setApiResponse(response);
			logger.debug("End : bulkStatusCheck response :: {}", response);
			return Mono.just(new ResponseEntity<>(responseWrapper, HttpStatus.OK));
		});
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
	})
	@Operation(summary = "Maitri UPI Loan Collection", description = "Post loan collection via UPI")
	@PutMapping(value = "/maitriUpiCollection", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> maitriUpiCollection(
			@RequestBody MaitriUPIRequestWrapper requestWrapper,
			@RequestHeader String appId,
			@RequestHeader String interfaceId,
			@RequestHeader String userId,
			@RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) {

		logger.warn("Start : maitriUpiCollection with request :: {}", requestWrapper);

		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header value :: {}", header);

		MaitriUPIRequest apiRequest = requestWrapper.getApiRequest();
		Mono<Response> responseMono = upiPaymentService.maitriUpiCollection(apiRequest, header);

		return responseMono.map(response -> {
			ResponseWrapper responseWrapper = new ResponseWrapper();
			responseWrapper.setApiResponse(response);

			logger.debug("End : maitriUpiCollection response :: {}", response);

			return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
		});
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch QR details by kendra & meeting date", description = "Fetch QR details by kendra & meeting date")
	@PostMapping(value = "/fetchQRDetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> fetchQRDetails(@RequestBody FetchQRDetailsRequestWrapper requestWrapper) {
		logger.warn("Start : fetchQRDetails with request :: {}", requestWrapper);
//		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
//		logger.debug("Header value :: {}", header);

		FetchQRDetailsRequest apiRequest = requestWrapper.getApiRequest();
		Response response = upiPaymentService.fetchQRDetails(apiRequest);

		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		logger.debug("End : fetchQRDetails response :: {}", response);

		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch & update customer transaction details", description = "Fetch & update customer transaction details")
	@PostMapping(value = "/fetchCustomerTxnDetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> fetchCustomerTxnDetails(@RequestBody FetchCustomerTxnDetailsRequestWrapper requestWrapper,
																		 @RequestHeader String appId, @RequestHeader String interfaceId,
																		 @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
																		 @RequestHeader String deviceId) {
		logger.warn("Start : fetchCustomerTxnDetails with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header value :: {}", header);

		FetchCustomerTxnDetailsRequest apiRequest = requestWrapper.getApiRequest();
		Mono<Response> responseMono = upiPaymentService.fetchCustomerTxnDetails(apiRequest, header);
		return responseMono.map(response -> {
			ResponseWrapper responseWrapper = new ResponseWrapper();
			responseWrapper.setApiResponse(response);
			logger.debug("End : fetchCustomerTxnDetails response :: {}", response);
			return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
		});
	}
}
