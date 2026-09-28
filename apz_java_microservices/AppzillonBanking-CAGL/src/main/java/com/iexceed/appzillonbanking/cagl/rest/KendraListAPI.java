package com.iexceed.appzillonbanking.cagl.rest;

import java.util.List;

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
import com.iexceed.appzillonbanking.cagl.dto.KendraNameIdDto;
import com.iexceed.appzillonbanking.cagl.payload.KendraListRequestWrapper;
import com.iexceed.appzillonbanking.cagl.payload.KendraListResponse;
import com.iexceed.appzillonbanking.cagl.payload.KendraListResponseBody;
import com.iexceed.appzillonbanking.cagl.payload.KendraListResponseWrapper;
import com.iexceed.appzillonbanking.cagl.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cagl.service.KendraListService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(description = "application/cob", name = "KendraList")
@RequestMapping("application/cob")
public class KendraListAPI {

	private static final Logger logger = LogManager.getLogger(KendraListAPI.class);

	@Autowired
	private KendraListService kendraListService;

	@ApiResponses({ @ApiResponse(responseCode = "200", description = "AppzillonBanking API reachable"),
			@ApiResponse(responseCode = "408", description = "Service Timed Out"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "404", description = "AppzillonBanking not reachable") })
	@Operation(summary = "Fetch Kendra list", description = "API to fetch kendra id and kendra name for the given list of branch ids, optionally filtered by a list of kendra ids")
	@PostMapping(value = "/fetchKendraList", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<KendraListResponseWrapper> fetchKendraListApi(
			@RequestBody KendraListRequestWrapper reqWrapper) {
		KendraListResponse resp;
		try {
			logger.debug("Start: Fetch Kendra list: {}", reqWrapper);
			List<KendraNameIdDto> kendraList = kendraListService
					.fetchKendraListByBranch(reqWrapper.getApiRequest().getRequestObj());
			KendraListResponseBody respBody = KendraListResponseBody.builder().kendraList(kendraList).build();
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage(CommonConstants.RESP_SUCCESS_STATUS).build();
			resp = KendraListResponse.builder().responseHeader(header).responseBody(respBody).build();
		} catch (Exception e) {
			logger.error(CommonConstants.EXCEP_OCCURED, e);
			ResponseHeader header = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			resp = KendraListResponse.builder().responseHeader(header).build();
		}
		KendraListResponseWrapper wrapper = KendraListResponseWrapper.builder().apiResponse(resp).build();
		logger.debug("End: Fetch Kendra list: {}", wrapper);
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
	}

}
