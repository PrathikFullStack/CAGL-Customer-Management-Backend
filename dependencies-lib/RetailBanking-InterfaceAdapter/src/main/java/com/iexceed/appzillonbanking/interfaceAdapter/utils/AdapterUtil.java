package com.iexceed.appzillonbanking.interfaceAdapter.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iexceed.appzillonbanking.core.constants.CommonConstants;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.parser.ResponseParser;

import reactor.core.publisher.Mono;

@Component
public class AdapterUtil {

	@Autowired
	private ExternalIntReqResMapper externalIntReqResMapper;

	@Autowired
	private JSONResponseMapper responseMapper;

	@Autowired
	private ParserUtils parserUtils;

	@Autowired
	private ResponseParser responseParser;

	private static final Logger logger = LogManager.getLogger(AdapterUtil.class);

	public JSONObject setSuccessResp(String apiResponse) {
		logger.debug("Start : setSuccessResp");
		JSONObject response = new JSONObject();
		response.put(CommonConstants.ERR_CODE, "0");
		response.put(CommonConstants.ERR_MESSAGE, "");
		response.put(CommonConstants.RESPONSE_OBJ, apiResponse);
		logger.debug("End : setSuccessResp" + response);
		return response;
	}

	/**
	 * Method to set Mono<Object> for successful response from the API.
	 * 
	 * @author akshay.upadhya
	 * @since 15.11.2021
	 * @param apiResponse
	 * @return
	 */
	public Mono<Object> setSuccessRespMono(String apiResponse) {
		logger.debug("Start : setSuccessResp, apiResponse = " + apiResponse);
		JSONObject response = new JSONObject();
		response.put(CommonConstants.ERR_CODE, "0");
		response.put(CommonConstants.ERR_MESSAGE, "");
		response.put(CommonConstants.RESPONSE_OBJ, apiResponse);
		return Mono.just(response);
	}

	public JSONObject setError(String errorMsg, String errorCode) {
		logger.debug("Start : setError");
		JSONObject response = new JSONObject();
		response.put(CommonConstants.ERR_CODE, errorCode);
		response.put(CommonConstants.ERR_MESSAGE, errorMsg);
		response.put(CommonConstants.RESPONSE_OBJ, "");
		logger.debug("End : setError" + response);
		return response;
	}

	/**
	 * Method to generate a Mono<Object> for setting the error case condition
	 * 
	 * @author akshay.upadhya
	 * @since 15.11.2021
	 * @param errorMsg
	 * @param errorCode
	 * @return
	 */
	public Mono<Object> setErrorMono(String errorMsg, String errorCode) {
		logger.debug("Start : setError");
		JSONObject response = new JSONObject();
		response.put(CommonConstants.ERR_CODE, errorCode);
		response.put(CommonConstants.ERR_MESSAGE, errorMsg);
		response.put(CommonConstants.RESPONSE_OBJ, "");
		logger.debug("End : setError" + response);
		return Mono.just(response);
	}

	/**
	 * Generic Method to return the Mono<ResponseEntity> wrapper for the external
	 * API service call.
	 * 
	 * @author akshay.upadhya
	 * @since 15.11.2021
	 * @param responseObject
	 * @param intfFileName
	 * @param header
	 * @return
	 */
	public Mono<ResponseEntity<ResponseWrapper>> generateResponseWrapper(Mono<Object> responseObject,
			String intfFileName, Header header, boolean isJSONAdapterCall) {
		logger.debug("End : generateResponseWrapper method with response :: " + responseObject.toString());
		return responseObject.map(val -> {
			ResponseWrapper responseWrapper = this.getResponseMapper(val, intfFileName, header, isJSONAdapterCall);
			return ResponseEntity.status(HttpStatus.OK).body(responseWrapper);
		});
	}

	public Mono<ResponseWrapper> generateRespWrapper(Mono<Object> responseObj, String intfFileName, Header header,
			boolean isJSONAdapterCall) {
		logger.debug("End : generateRespWrapper Wrapper :: " + responseObj.toString());
		return responseObj.map(val -> {
			ResponseWrapper responseWrapper = this.getResponseMapper(val, intfFileName, header, isJSONAdapterCall);
			return responseWrapper;
		});
	}

	public ResponseWrapper getResponseMapper(Object val, String interfaceFileName, Header header,
			boolean isJSONAdapterCall) {
		String extApiResponse = val.toString();
		logger.debug("Response from API::" + extApiResponse);
		// If val is an instanceof HashMap converting to String.
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		try {
			if (val instanceof Response) {
				logger.debug("Response is an instance of Response POJO, Hence adding the wrapper and returning as is");
				String extApiResp = new ObjectMapper().writeValueAsString(val);
				responseBody.setResponseObj("");
				response.setResponseBody(responseBody);
				responseWrapper.setApiResponse(new Gson().fromJson(extApiResp, Response.class));
				logger.debug("responseWrapper::" + responseWrapper);
				return responseWrapper;
			} else if (val instanceof HashMap<?, ?> || val instanceof ArrayList<?>) {
				extApiResponse = new ObjectMapper().writeValueAsString(val);
			}
			if (extApiResponse != null && !extApiResponse.trim().equals("")) {
				String interfaceFileContent = null;
				if (isJSONAdapterCall) {
					interfaceFileContent = parserUtils.readInterfaceJSONFileContentFromServer(interfaceFileName);
				} else {
					interfaceFileContent = parserUtils.readInterfaceContentFromServer(interfaceFileName);
				}
				this.checkAPIResponseFormat(extApiResponse, interfaceFileContent, responseHeader, responseBody,
						isJSONAdapterCall, interfaceFileName);
			} else {
				logger.debug("empty response received");
				responseBody.setResponseObj("");
				responseHeader.setErrorCode("7");
				responseHeader.setResponseCode(CommonConstants.FAILURE);
				responseHeader.setResponseMessage("Empty response received from the API");
			}
		} catch (Exception e) {
			logger.error("Exception Occured::", e);
			responseBody.setResponseObj("");
			CommonUtils.generateHeaderForGenericError(responseHeader);
		}
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		responseWrapper.setApiResponse(response);
		return responseWrapper;
	}

	private void checkAPIResponseFormat(String extApiResponse, String interfaceResponseObject,
			ResponseHeader responseHeader, ResponseBody responseBody, boolean isJSONAdapterCall,
			String interfaceFileName) {
		if (extApiResponse.startsWith("{") && extApiResponse.endsWith("}")) {
			JSONObject responseParam = new JSONObject(extApiResponse);
			logger.debug("External API execution is successful.");
			if (responseParam.has(CommonConstants.API_RESPONSE)
					&& responseParam.getJSONObject(CommonConstants.API_RESPONSE).has(CommonConstants.RESPONSE_BODY)) {
				this.apiResponseWrapperForInternalCall(responseBody, responseHeader, responseParam);
			} else {
				extApiResponse = this.apiResponseMapper(extApiResponse, interfaceResponseObject, isJSONAdapterCall,
						interfaceFileName);
				responseBody.setResponseObj(extApiResponse);
				CommonUtils.generateHeaderForSuccess(responseHeader);
			}
		} else if (extApiResponse.startsWith("[{") && extApiResponse.endsWith("}]")) {
			logger.debug("External API execution is successful and response is an Array.");
			responseBody.setResponseObj(extApiResponse);
			CommonUtils.generateHeaderForSuccess(responseHeader);

		} else if ("[]".equalsIgnoreCase(extApiResponse) || "{}".equalsIgnoreCase(extApiResponse)) {
			logger.debug("empty response received");
			responseBody.setResponseObj(extApiResponse);
			responseHeader.setErrorCode("8");
			responseHeader.setResponseCode(CommonConstants.SUCCESS);
			responseHeader.setResponseMessage("Empty Object response received from the API");
		} else {
			logger.debug("Else case handler for response parser.");
			responseBody.setResponseObj("");
			CommonUtils.generateHeaderForGenericError(responseHeader);
		}
	}

	private void apiResponseWrapperForInternalCall(ResponseBody responseBody, ResponseHeader responseHeader,
			JSONObject responseParam) {
		responseBody.setResponseObj("");
		responseHeader.setErrorCode("");
		JSONObject responseBodyJSON = responseParam.getJSONObject("apiResponse").getJSONObject("ResponseBody");
		if (responseBodyJSON.get("responseObj").toString() != null
				|| !"".equalsIgnoreCase(responseBodyJSON.get("responseObj").toString().trim())
						&& CommonUtils.isJSONValid(responseBodyJSON.get("responseObj").toString())) {
			responseBody.setResponseObj(responseBodyJSON.get("responseObj").toString());
		}
		responseHeader.setResponseCode(
				responseParam.getJSONObject("apiResponse").getJSONObject("ResponseHeader").getString("ResponseCode"));
		responseHeader.setResponseMessage(responseParam.getJSONObject("apiResponse").getJSONObject("ResponseHeader")
				.getString("ResponseMessage"));
	}

	private String apiResponseMapper(String extApiResponse, String externalIntfJSON, boolean isJSONAdapterCall,
			String interfaceFileName) {
		JSONObject restApiResponse = this.setSuccessResp(extApiResponse);
		if ((restApiResponse.get(CommonConstants.ERR_CODE).toString()).equalsIgnoreCase("0")) {
			logger.debug("api execution is completed, Calling response modification method");
			String parsedResponse = responseParser
					.parseAPIResponse(restApiResponse.get(CommonConstants.RESPONSE_OBJ).toString(), interfaceFileName);
			logger.debug("Response Modification completed, Starting the response parsing logic::");
			if (isJSONAdapterCall) {
				return this.formatAPIResponseFromExternalJSONFile(externalIntfJSON, parsedResponse, extApiResponse);
			} else {
				JSONObject interfaceJsonContent = new JSONObject(externalIntfJSON);
				JSONObject interfaceResponseObject = parserUtils.obtainResponseObject(interfaceJsonContent);
				logger.debug("interfaceResponseObject = ", interfaceResponseObject);
				// Handling for the JSON Object as response.
				return this.formatAPIResponseFromInterfaceFile(interfaceResponseObject, parsedResponse, extApiResponse);
			}
		}
		return extApiResponse;
	}

	/**
	 * Method to Format the API Response based on the External Interface file.
	 * 
	 * @param interfaceResponseObject Content of the external Interface file as
	 *                                {@code JSONObject}
	 * @param restApiResponse         Updated Rest API Response
	 * @param extApiResponse          External REST Service Response in
	 *                                {@code String} format.
	 * @param interfaceFileName
	 * @return Returns the modified response in {@code String} format.
	 */
	private String formatAPIResponseFromInterfaceFile(JSONObject interfaceResponseObject, String restApiResponse,
			String extApiResponse) {
		parserUtils.reinitializeGlobalVariables();
		if ((interfaceResponseObject.has("childs"))
				&& ((restApiResponse != null) && !restApiResponse.trim().equals(""))) {
			logger.debug("Response data model is configured for this interface, setting the required api response");
			if (CommonUtils.isJSONValid(restApiResponse)) {
				JSONObject finalRespObj = new JSONObject();
				try {
					finalRespObj = externalIntReqResMapper
							.obtainRequiredRestApiResponse(new JSONObject(restApiResponse), interfaceResponseObject);
				} catch (Exception e) {
					logger.error("Exception Occured:", e);
				}
				return finalRespObj.toString();
			} else {
				logger.debug("Client Response is not in json format, hence returning the entire response");
			}
		} else {
			logger.debug("Response data model is not configured, setting the entire api response");
		}
		return extApiResponse;
	}

	/**
	 * Method to Format the API Response based on the External JSON file.
	 * 
	 * @param externalIntfJSON  Content of the external JSON definition file as
	 *                          {@code JSONObject}
	 * @param restApiResponse   Updated Rest API Response
	 * @param extApiResponse    External REST Service Response in {@code String}
	 *                          format.
	 * @param interfaceFileName
	 * @return Returns the modified response in {@code String} format.
	 */
	private String formatAPIResponseFromExternalJSONFile(String externalIntfJSON, String restApiResponse,
			String extApiResponse) {
		if ((null != restApiResponse || !"".equals(restApiResponse.trim()))
				&& CommonUtils.isJSONValid(restApiResponse)) {
			org.codehaus.jettison.json.JSONObject finalRespObj = new org.codehaus.jettison.json.JSONObject();
			try {

				finalRespObj = responseMapper.responseParsingLogic(externalIntfJSON, extApiResponse,
						new org.codehaus.jettison.json.JSONObject(restApiResponse));
			} catch (Exception e) {
				logger.error("Exception Occured:", e);
			}
			return finalRespObj.toString();
		}
		return extApiResponse;
	}
}
