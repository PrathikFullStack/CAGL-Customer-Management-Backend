package com.iexceed.appzillonbanking.cagl.rest;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.payload.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.constants.CommonConstants;
import com.iexceed.appzillonbanking.cagl.dto.CollectionsData;
import com.iexceed.appzillonbanking.cagl.entity.DigitalCollection;
import com.iexceed.appzillonbanking.cagl.entity.ServerDate;
import com.iexceed.appzillonbanking.cagl.repository.cus.ServerDateRepo;
import com.iexceed.appzillonbanking.cagl.service.CollectionsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(description = "application/collection", name = "Collections")
@RequestMapping("application/collection")
public class CollectionsAPI {

	private static final Logger logger = LogManager.getLogger(CollectionsAPI.class);

	@Autowired
	private CollectionsService collectionsService;
	
	@Autowired
	private ServerDateRepo dateRepo;

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch Collection details", description = "API to Fetch Collection details")
	@PostMapping(value = "/cdh/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<KendraResponseWrapper> fetchKendraCustomerDetails(
			@RequestBody KendraRequestWrapper reqWrapper) {
		Response resp = null;
		try {
			logger.debug("Start: Fetch Collection details:{}", reqWrapper);
			List<CollectionsData> collectionsData = collectionsService
					.getCollectionDtlsListByKendra(reqWrapper.getApiRequest().getRequestObj());
			String serverDt = "";
			List<ServerDate> serverDateList = dateRepo.findAll();
			if (!serverDateList.isEmpty()) {
				serverDt = serverDateList.get(0).getCurrentDate();
			}
			ResponseBody respBody = ResponseBody.builder().responseObj(null).collectionsObj(collectionsData)
					.serverDate(serverDt).build();
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage(CommonConstants.RESP_SUCCESS_STATUS).build();
			resp = Response.builder().responseBody(respBody).responseHeader(header).build();
		} catch (Exception e) {
			logger.error(CommonConstants.EXCEP_OCCURED, e);
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			resp = Response.builder().responseHeader(header).build();
		}
		KendraResponseWrapper wrapper = KendraResponseWrapper.builder().apiResponse(resp).build();
		logger.debug("End: Fetch Collection details:{}", wrapper);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch mahi collection details", description = "API to Fetch mahi collection details")
	@PostMapping(value = "/cdh/fetch/mahi", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FetchMahiResponseWrapper> fetchMahiCollectionDetails(
			@RequestBody FetchMahiRequestWrapper reqWrapper) {
		FetchMahiResponse resp = null;
		try {
			logger.debug("Start: Fetch mahi collection details:{}", reqWrapper);
			List<DigitalCollection> mahiCollectionList = collectionsService
					.fetchMahiCollectionDetails(reqWrapper.getApiRequest().getRequestObj());
			FetchMahiResponseBody respBody = FetchMahiResponseBody.builder().responseObj(null)
					.collectionsObj(mahiCollectionList).build();
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage(CommonConstants.RESP_SUCCESS_STATUS).build();
			resp = FetchMahiResponse.builder().responseBody(respBody).responseHeader(header).build();
		} catch (Exception e) {
			logger.error(CommonConstants.EXCEP_OCCURED, e);
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			resp = FetchMahiResponse.builder().responseHeader(header).build();
		}
		FetchMahiResponseWrapper wrapper = FetchMahiResponseWrapper.builder().apiResponse(resp).build();
		logger.debug("End: Fetch mahi collection details:{}", wrapper);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
	}


	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch mahi collection details", description = "API to Fetch mahi collection details by branch")
	@PostMapping(value = "/cdh/fetch/mahiByBranch", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FetchMahiResponseWrapper> fetchMahiCollectionByBranch(
			@RequestBody FetchMahiByBranchRequestWrapper reqWrapper) {
		FetchMahiResponse response;
		try {
			logger.debug("Start: Fetch mahi collection by branch: {}", reqWrapper);
			List<DigitalCollection> collectionList = collectionsService
					.fetchMahiCollectionByBranch(reqWrapper.getApiRequest().getRequestObj());
			FetchMahiResponseBody respBody = FetchMahiResponseBody.builder().responseObj(null)
					.collectionsObj(collectionList).build();
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage(CommonConstants.RESP_SUCCESS_STATUS).build();
			response = FetchMahiResponse.builder().responseHeader(header).responseBody(respBody).build();
		} catch (Exception e) {
			logger.error(CommonConstants.EXCEP_OCCURED, e);
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			response = FetchMahiResponse.builder().responseHeader(header).build();
		}
		FetchMahiResponseWrapper wrapper = FetchMahiResponseWrapper.builder().apiResponse(response).build();
		logger.debug("End: Fetch mahi collection by branch: {}", wrapper);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch & check mahi collection details", description = "API to Fetch and check mahi collection details by customerId")
	@PostMapping(value = "/cdh/fetch/checkMahi", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FetchMahiResponseWrapper> fetchAndCheckMahiCollection(
			@RequestBody CheckMahiByCustomerRequestWrapper reqWrapper) {
		FetchMahiResponse response;
		try {
			logger.debug("Start: Fetch and check mahi collection by customerId: {}", reqWrapper);
			List<DigitalCollection> collectionList = collectionsService
					.checkMahiCollection(reqWrapper.getApiRequest().getRequestObj());
			FetchMahiResponseBody respBody = FetchMahiResponseBody.builder().responseObj(null)
					.collectionsObj(collectionList).build();
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage(CommonConstants.RESP_SUCCESS_STATUS).build();
			response = FetchMahiResponse.builder().responseHeader(header).responseBody(respBody).build();
		} catch (Exception e) {
			logger.error(CommonConstants.EXCEP_OCCURED, e);
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			response = FetchMahiResponse.builder().responseHeader(header).build();
		}
		FetchMahiResponseWrapper wrapper = FetchMahiResponseWrapper.builder().apiResponse(response).build();
		logger.debug("End: Fetch and check mahi collection by customerId: {}", wrapper);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
	}
}
