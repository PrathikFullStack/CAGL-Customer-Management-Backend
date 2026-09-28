package com.iexceed.appzillonbanking.cagl.cob.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.iexceed.appzillonbanking.cagl.cob.service.OnboardingAmlBreService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateKendraRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateKendraRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.DmsDocumentRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.PhotoThumbnailRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.DMSService;
import com.iexceed.appzillonbanking.cagl.cob.service.OnboardingService;
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
import com.iexceed.appzillonbanking.cagl.cob.payload.TransferApplicationRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import com.iexceed.appzillonbanking.cagl.cob.payload.AmlBreCheckRequestWrapper;

@RestController
@Tag(description = "application/cob", name = "ON BOARDING")
@RequestMapping("application/cob")
public class OnboardingController {
	private static final Logger logger = LogManager.getLogger(OnboardingController.class);

	@Autowired
	private DMSService dmsService;

	@Autowired
    OnboardingService onboardingService;

	@Autowired
	private AdapterUtil adapterUtil;

	@Autowired
	private OnboardingAmlBreService onboardingAmlBreService;


	@ApiResponses({ @ApiResponse(responseCode = "200", description = "DMS Document Uploaded Successfully"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "DMS Service Not Reachable") })
	@Operation(summary = "Upload DMS Document", description = "API to upload document into DMS")
	@PostMapping(value = "/dms", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> uploadDMSDocument(@RequestHeader String appId,
			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId, @RequestBody DmsDocumentRequest apiRequest) throws IOException {
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("uploadDMSDocument Header value :: {}", header);
		return dmsService.processDMSDoc(apiRequest, header)
				.map(responseWrapper -> ResponseEntity.ok(responseWrapper)).onErrorResume(e -> {
					logger.error("Error while uploading DMS document", e);
					ResponseWrapper responseWrapper = new ResponseWrapper();
					Response response = new Response();
					ResponseHeader responseHeader = new ResponseHeader();
					ResponseBody responseBody = new ResponseBody();
					CommonUtils.generateHeaderForFailure(responseHeader, "Failed to upload DMS document");
					responseBody.setResponseObj("");
					response.setResponseBody(responseBody);
					response.setResponseHeader(responseHeader);
					responseWrapper.setApiResponse(response);
					return Mono.just(ResponseEntity.ok(responseWrapper));
				});
	}
//
//	@ApiResponses({
//			@ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
//			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
//			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
//			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")})
//	@Operation(summary = "Create Kendra", description = "API for creating Kendra")
//	@PostMapping(value = "/kendra/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<ResponseWrapper> createKendra(
//			@RequestBody CreateKendraRequestWrapper requestWrapper, @RequestHeader String appId,
//			@RequestHeader String interfaceId, @RequestHeader String userId, @RequestHeader String masterTxnRefNo,
//			@RequestHeader String deviceId) {
//		logger.info("Start : createKendra :: {}", requestWrapper);
//		ResponseWrapper responseWrapper = new ResponseWrapper();
//		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
//		logger.debug("Header :: {}", header);
//		CreateKendraRequest request = requestWrapper.getApiRequest();
//		Response response = onboardingService.createKendra(request, header);
//		responseWrapper.setApiResponse(response);
//		logger.info("End : createKendra :: {}", response);
//		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
//	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Photo Thumbnails Fetched Successfully"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "Service Not Reachable") })
	@Operation(summary = "Get Photo Thumbnails", description = "API to fetch photo thumbnails for given application IDs")
	@PostMapping(value = "/photo-thumbnails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> getPhotoThumbnails(@RequestBody PhotoThumbnailRequestWrapper requestWrapper,
			@RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
			@RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.info("Start : getPhotoThumbnails :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header :: {}", header);

		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		try {
			List<String> applicationIds = requestWrapper.getApiRequest().getRequestObj().getApplicationIds();
			String docuType = requestWrapper.getApiRequest().getRequestObj().getSubType();
			Map<String, String> thumbnails = dmsService.getThumbnails(applicationIds, docuType);

			String thumbnailsJson = new Gson().toJson(thumbnails);
			responseBody.setResponseObj(thumbnailsJson);
			response.setResponseBody(responseBody);
			response.setResponseHeader(responseHeader);
			responseWrapper.setApiResponse(response);

		} catch (Exception e) {
			logger.error("Error while fetching photo thumbnails", e);
			CommonUtils.generateHeaderForFailure(responseHeader, "Failed to fetch photo thumbnails");
			responseBody.setResponseObj("");
			response.setResponseBody(responseBody);
			response.setResponseHeader(responseHeader);
			responseWrapper.setApiResponse(response);
		}

		logger.info("End : getPhotoThumbnails :: {}", responseWrapper);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Onboarding Check cb", description = "API for onboarding cbcheck")
	@PostMapping(value = "/onboardingCbcheck", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> onboardingCbCheck(@RequestBody CbCheckRequestWrapper requestWrapper,
																   @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
																   @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		String schedulerFlag = requestWrapper.getApiRequest().getRequestObj().getSchedulerEnabled();
		if (schedulerFlag != null && schedulerFlag.equalsIgnoreCase("Y")) {
			logger.debug("Onboarding CB check API call for Scheduler started for the application ID "
					+ requestWrapper.getApiRequest().getRequestObj().getApplicationId());
		} else {
			logger.debug("Onboarding CB check API call for Application started");
		}
		logger.warn("Start : Onboarding cbCheck with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Onboarding cbCheck Header value :: {} ", header);
		CbRequest cbCheckRequest = requestWrapper.getApiRequest();
		Mono<Object> response = onboardingService.onboardingCbCheck(cbCheckRequest, header, schedulerFlag);
	   logger.debug("Onboarding CBCheck response :: {} ", response);
	   return adapterUtil.generateResponseWrapper(response, cbCheckRequest.getInterfaceName(), header, true);
	}


	//MBDF transfer controller
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Transfer Application", description = "API for transferring members or a whole group")
	@PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> transferApplication(@RequestBody TransferApplicationRequest request,
															   @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
															   @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.info("Start : transferApplication :: {}", request);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Header :: {}", header);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response response = onboardingService.transferApplication(request);
		responseWrapper.setApiResponse(response);
		logger.info("End : transferApplication :: {}", response);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Application lock API successful"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
	})
	@Operation(summary = "Lock Application", description = "API to acquire or refresh application lock for RPC user")
	@PostMapping(value = "/lockApplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> lockApplication(
			@RequestBody RecordLockRequestWrapper requestWrapper,
			@RequestHeader String appId,
			@RequestHeader String interfaceId,
			@RequestHeader String userId,
			@RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) throws Exception {
		logger.warn("Start : lockApplication with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Lock Application Header value :: {}", header);
		RecordLockRequestFields request = requestWrapper.getRequestObj();
		onboardingService.lockApplication(request.getApplicationId(), request.getUserId(), request.getUserRole());
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setHttpStatus(HttpStatus.OK);
		responseHeader.setResponseMessage("Application locked successfully.");
		ResponseBody responseBody = new ResponseBody();
		responseBody.setResponseObj(
				"{\"applicationId\":\""
						+ request.getApplicationId()
						+ "\"}"
		);
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		return Mono.just(ResponseEntity.ok(new ResponseWrapper(response)));
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Application unlock API successful"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable")
	})
	@Operation(summary = "Unlock Application", description = "API to release application lock for RPC user")
	@PostMapping(value = "/unlockApplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> unlockApplication(
			@RequestBody RecordLockRequestWrapper requestWrapper,
			@RequestHeader String appId,
			@RequestHeader String interfaceId,
			@RequestHeader String userId,
			@RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId) throws Exception {
		logger.warn("Start : unlockApplication with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Unlock Application Header value :: {}", header);
		RecordLockRequestFields request = requestWrapper.getRequestObj();
		onboardingService.unlockApplication(request.getApplicationId(), request.getUserId(), request.getUserRole());
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setHttpStatus(HttpStatus.OK);
		responseHeader.setResponseMessage("Application unlocked successfully.");
		ResponseBody responseBody = new ResponseBody();
		responseBody.setResponseObj(
				"{\"applicationId\":\""
						+ request.getApplicationId()
						+ "\"}"
		);
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		return Mono.just(ResponseEntity.ok(new ResponseWrapper(response)));
	}

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Onboarding AML BRE Check", description = "API for onboarding combined AML and BRE check")
	@PostMapping(value = "/onboardingAmlBreCheck", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> onboardingAmlBreCheck(@RequestBody AmlBreCheckRequestWrapper requestWrapper,
																	   @RequestHeader String appId, @RequestHeader String interfaceId, @RequestHeader String userId,
																	   @RequestHeader String masterTxnRefNo, @RequestHeader String deviceId) {
		logger.debug("Onboarding AML+BRE check API call started for applicationId: {}",
				requestWrapper.getApiRequest().getRequestObj().getApplicationId());
		logger.warn("Start : Onboarding AmlBreCheck with request :: {}", requestWrapper);
		Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("Onboarding AmlBreCheck Header value :: {} ", header);
		AmlBreCheckRequest amlBreCheckRequest = requestWrapper.getApiRequest();
		Mono<Object> response = onboardingAmlBreService.processAmlBreCheck(amlBreCheckRequest, header);
		logger.debug("Onboarding AmlBreCheck response :: {} ", response);
		return adapterUtil.generateResponseWrapper(response, amlBreCheckRequest.getInterfaceName(), header, true);
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Video links fetched successfully"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "Video Service Not Reachable")
	})
	@Operation(summary = "Fetch Video Links", description = "API to fetch training video links for a given user and language")
	@PostMapping(value = "/videolinks", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> fetchVideoLinks(
			@RequestHeader String appId,
			@RequestHeader String interfaceId,
			@RequestHeader String userId,
			@RequestHeader String masterTxnRefNo,
			@RequestHeader String deviceId,
			@RequestBody VideoLinkRequest request) {

		com.iexceed.appzillonbanking.core.payload.Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
		logger.debug("fetchVideoLinks: header :: {}", header);
		return onboardingService.fetchVideoLinks(request, header)
				.map(ResponseEntity::ok)
				.onErrorResume(e -> {
					logger.error("fetchVideoLinks: error in controller", e);
					ResponseWrapper responseWrapper = new ResponseWrapper();
					Response response = new Response();
					ResponseHeader responseHeader = new ResponseHeader();
					ResponseBody responseBody = new ResponseBody();
					CommonUtils.generateHeaderForFailure(responseHeader, "Failed to fetch video links");
					responseBody.setResponseObj("");
					response.setResponseBody(responseBody);
					response.setResponseHeader(responseHeader);
					responseWrapper.setApiResponse(response);
					return Mono.just(ResponseEntity.ok(responseWrapper));
				});
	}

}
