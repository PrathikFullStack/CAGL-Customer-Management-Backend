package com.iexceed.appzillonbanking.interfaceAdapter.service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iexceed.appzillonbanking.core.constants.CommonConstants;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.ExternalIntReqResMapper;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.JSONAdapterUtil;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.JSONRequestMapper;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.ParserUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.URLMapper;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.URLUtils;

import reactor.core.publisher.Mono;

@Component
public class HttpInterfaceParser {

	@Autowired
	private RestService restService;

	@Autowired
	private ExternalIntReqResMapper externalInterfaceResponseMapper;

	@Autowired
	private AdapterUtil adapterUtil;

	@Autowired
	private JSONAdapterUtil jsonAdapterUtil;

	@Autowired
	private ParserUtils parserUtils;

	@Autowired
	private JSONRequestMapper requestMapper;

	@Autowired
	private URLMapper urlMapper;

	@Autowired
	private URLUtils urlUtils;

	private static final Logger logger = LogManager.getLogger(HttpInterfaceParser.class);
	private static final String ERROR_CONST = "Error occurred while executing the rest service, error = ";

	private static final String PATH_PARAMETERS_LEFT_FILLER = "{";
	private static final String PATH_PARAMETERS_RIGHT_FILLER = "}";
	private static final String API_URL_TXT = "apiURL";

	/**
	 * Method to call the external Service based on the Appzillon Interface
	 * Definition file by constructing request, header and url parameters.
	 * 
	 * @param request              Front End Request in {@code JSONObject}
	 * @param interfaceJsonContent Interface file contents as {@code JSONObject}
	 * @param header               Request Header Parameters
	 * @return External API Response as {@code Mono<Object>}
	 */
	public Mono<Object> callService(JSONObject request, JSONObject interfaceJsonContent, Header header,
			String interfaceName) {
		logger.debug("Start : callService with request = " + request.toString());
		JSONObject nestedJson = new JSONObject();
		String restRequest = nestedJson.toString();

		try {

			String httpActionType = interfaceJsonContent.get("httpactiontype").toString();
			logger.debug("httpActionType = " + httpActionType);

			JSONObject requiredRestObject = parserUtils.obtainRequestObject(interfaceJsonContent);
			logger.debug("requiredRestObject = " + requiredRestObject);

			parserUtils.reinitializeGlobalVariables();

			Map<String, ArrayList<String>> finalMap = new LinkedHashMap<String, ArrayList<String>>();
			finalMap = parserUtils.obtainInterfaceNodes(requiredRestObject);

			LinkedHashMap<String, ArrayList<String>> elemestsMap = new LinkedHashMap<String, ArrayList<String>>();
			elemestsMap = externalInterfaceResponseMapper.obtainRestInterfaceNodes(requiredRestObject);

			finalMap.putAll(elemestsMap);
			logger.debug("finalMap = " + finalMap);

			List<JSONObject> parsedList = new ArrayList<JSONObject>();
			parsedList = parserUtils.obtainInterfaceElements(requiredRestObject);
			JSONArray parsedArray = new JSONArray(parsedList.toString());
			logger.debug("parsedArray = " + parsedArray);

			// Setting the content type of the api
			String contentType = obtainContentType(interfaceJsonContent);
			logger.debug("contentType = " + contentType);

			/**
			 * Code change to allow direct service call without request mapping in the
			 * external interface Added to support list of objects for external interface
			 * services.
			 */
			if (parsedArray.length() == 0 && "application/json".equalsIgnoreCase(contentType)) {
				if (request.getJSONObject(CommonConstants.API_REQUEST).get("requestObj") instanceof JSONObject) {
					nestedJson = request.getJSONObject(CommonConstants.API_REQUEST).getJSONObject("requestObj");
					restRequest = nestedJson.toString();
				} else if (request.getJSONObject(CommonConstants.API_REQUEST).get("requestObj") instanceof JSONArray) {
					restRequest = request.getJSONObject(CommonConstants.API_REQUEST).getJSONArray("requestObj")
							.toString();
				}
			} else {
				if (request.getJSONObject(CommonConstants.API_REQUEST).get("requestObj") instanceof JSONObject) {
					JSONObject simpleJson = externalInterfaceResponseMapper.obtainSimpleRestJson(request, parsedArray);
					logger.debug("simpleJson = " + simpleJson);

					nestedJson = parserUtils.obtainNestedJson(simpleJson, finalMap);
					logger.debug("nestedJson = " + nestedJson);
					restRequest = nestedJson.toString();
				} else if (request.getJSONObject(CommonConstants.API_REQUEST).get("requestObj") instanceof JSONArray) {
					restRequest = request.getJSONObject(CommonConstants.API_REQUEST).getJSONArray("requestObj")
							.toString();
				}
			}
			String endPointUrl = interfaceJsonContent.get("endpointurl").toString();
			this.getUpdatedDefaulInterfaceURL(interfaceName, interfaceJsonContent);

			logger.debug("endPointUrl = " + endPointUrl);

			// Setting the API Headers
			JSONArray headerParams = obtainHeaderParams(interfaceJsonContent);
			logger.debug("headerParams = " + headerParams);

			// Setting the Path Parameters
			endPointUrl = obtainPathParams(interfaceJsonContent,request, endPointUrl, parsedArray);
			logger.debug("endPointUrl after adding path params = " + endPointUrl);

			// Setting the Query Parameters
			endPointUrl = obtainQueryParams(interfaceJsonContent, request, endPointUrl, parsedArray);
			logger.debug("endPointUrl after adding query params = " + endPointUrl);

			if (request.getJSONObject(CommonConstants.API_REQUEST).has("arrayObj")
					&& null != request.getJSONObject(CommonConstants.API_REQUEST).getJSONArray("arrayObj")
					&& request.getJSONObject(CommonConstants.API_REQUEST).getJSONArray("arrayObj").length() > 0) {
				restRequest = request.getJSONObject(CommonConstants.API_REQUEST).getJSONArray("arrayObj").toString();
			}
			return this.invokeExternalRestService(restRequest, headerParams, httpActionType, contentType, endPointUrl,
					interfaceJsonContent, header, false, interfaceName, request.toString());

		} catch (FileNotFoundException e) {
			logger.error(ERROR_CONST, e);
			return adapterUtil.setErrorMono("Please upload the interface file in the configured path.", "2");
		} catch (JSONException e) {
			logger.error(ERROR_CONST, e);
			return adapterUtil.setErrorMono("Request/Response parameters are missing.", "5");
		} catch (Exception e) {
			logger.error(ERROR_CONST, e);
			return adapterUtil.setErrorMono("Error in interface configuration", "1");
		}
	}

	public String obtainQueryParams(JSONObject interfaceJsonContent, JSONObject request, String endPointUrl,
			JSONArray parsedArray) {

		StringBuilder sb = new StringBuilder();
		Map<String, String> queryParams = externalInterfaceResponseMapper.obtainQueryParamData(request, parsedArray);
		logger.debug("queryParams = " + queryParams);
		int i = 0;
		logger.debug("endPointUrl before adding query params = " + endPointUrl);

		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			logger.debug("Creating the query parameters string");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			if (i != (queryParams.size() - 1)) {
				sb.append("&");
			}
			i++;
		}
		logger.debug("queryParamsBuilder string = " + sb.toString());
		if ((sb != null) && (sb.length() > 0)) {
			logger.debug("Appending the query parameters");

			if ((endPointUrl.substring(endPointUrl.length() - 1)).equalsIgnoreCase("/")) {
				logger.debug("removing the unwanted character");
				endPointUrl = endPointUrl.substring(0, endPointUrl.length() - 1) + "?" + sb.toString();
			} else {
				endPointUrl = endPointUrl + "?" + sb.toString();
			}
		}
		return endPointUrl;
	}

	public String obtainContentType(JSONObject interfaceJsonContent) {

		String contentType = interfaceJsonContent.get("reqcontenttype").toString();
		logger.debug("interface contentType = " + contentType);
		switch (contentType.toUpperCase()) {
		case "JSON":
			contentType = "application/json";
			break;
		case "XML":
			contentType = "application/xml";
			break;
		case "TEXT":
			contentType = "text/html";
			break;
		case "FORMDATA":
			contentType = "multipart/form-data";
			break;
		default:
			contentType = "application/json";
			break;
		}
		return contentType;
	}

	public JSONArray obtainHeaderParams(JSONObject interfaceJsonContent) {

		JSONArray headerParams = null;
		if (interfaceJsonContent.has("httpreqheaderparams")) {
			String headerParamsStr = interfaceJsonContent.get("httpreqheaderparams").toString();

			headerParams = new JSONArray(headerParamsStr);
			logger.debug("headerParams = " + headerParams);
		} else {
			logger.debug("Header parameters are not configured");
			headerParams = new JSONArray();
		}
		return headerParams;
	}

	public String obtainPathParams(JSONObject interfaceJsonContent, JSONObject request, String endPointUrl,
			JSONArray parsedArray) {

		logger.debug("endPointUrl before adding path params = " + endPointUrl);
		Map<String, String> pathParamsMap = externalInterfaceResponseMapper.obtainPathParamData(request, parsedArray);
		logger.debug("pathParamsMap = " + pathParamsMap);

		for (Map.Entry<String, String> entry : pathParamsMap.entrySet()) {
			String pathParamName = entry.getKey();
			logger.debug("pathParamName = " + pathParamName);

			String pathParamValue = entry.getValue();
			logger.debug("pathParamValue = " + pathParamValue);

			if ((endPointUrl.contains(PATH_PARAMETERS_LEFT_FILLER))
					&& (endPointUrl.contains(PATH_PARAMETERS_RIGHT_FILLER))) {
				int lfirstIndex = endPointUrl.indexOf(PATH_PARAMETERS_LEFT_FILLER);
				logger.debug("appendPathParameters lfirstIndex = " + lfirstIndex);
				int rfirstIndex = endPointUrl.indexOf(PATH_PARAMETERS_RIGHT_FILLER);
				logger.debug("appendPathParameters rfirstIndex = " + rfirstIndex);

				String pathParamToBeReplaced = endPointUrl.substring(lfirstIndex + 1, rfirstIndex);
				logger.debug("Path Param to be replaced = " + pathParamToBeReplaced);

				if (pathParamToBeReplaced.equalsIgnoreCase(pathParamName)) {
					endPointUrl = endPointUrl.replace(
							PATH_PARAMETERS_LEFT_FILLER + pathParamToBeReplaced + PATH_PARAMETERS_RIGHT_FILLER,
							pathParamValue);
				}
			}
		}
		return endPointUrl;
	}

	/**
	 * Method to invoke the external Rest Service.
	 * 
	 * @param restRequest          API request in {@code String} format.
	 * @param headerParams         Header Parameters as {@code JSONArray} with name
	 *                             and value pairs.
	 * @param httpActionType       HTTP Method Action
	 * @param contentType          HTTP Request Content Type
	 * @param endPointUrl          HTTP End-Point URL
	 * @param interfaceJsonContent Interface File Contents as {@code JSONObject}
	 * @param header               Request Header Parameters
	 * @return Response as {@code Mono<Object>}
	 */
	private Mono<Object> invokeExternalRestService(String restRequest, JSONArray headerParams, String httpActionType,
			String contentType, String endPointUrl, JSONObject interfaceJsonContent, Header header,
			Boolean isJSONAdapterCall, String interfaceName, String apiRequest) {
		try {
			// Executing the REST API
			return restService.executeRestApi(restRequest, headerParams, httpActionType, contentType, endPointUrl,
					interfaceJsonContent, header, isJSONAdapterCall, interfaceName, apiRequest);
		} catch (Exception e) {
			logger.error("Error occurred while executing the rest api, error = ", e);
			return adapterUtil.setErrorMono("Rest API execution failed", "4");
		}
	}

	/**
	 * Method to call External API based on reading the External API JSON Definition
	 * file.
	 * 
	 * @param request              API Request as {@code String} format
	 * @param interfaceJsonContent External API JSON Definition file in
	 *                             {@code String} format
	 * @param header               API Request Header Parameters
	 * @param interfaceName
	 * @return Returns a {@code Mono<Object>} of the external API Response
	 */
	public Mono<Object> callRestAPIService(String request, String interfaceJsonContent, Header header,
			String interfaceName) {
		logger.warn("Start : callRestAPIService with request = " + request);
		try {

			org.codehaus.jettison.json.JSONObject interfaceDefinitionObj = new org.codehaus.jettison.json.JSONObject(
					interfaceJsonContent);
			Map<String, Object> requestMap = jsonAdapterUtil.flattenJSONToMap(request);
			logger.warn("requestMap is :: {} " , requestMap);

			this.getUpdatedJSONInterfaceURL(interfaceDefinitionObj, interfaceName);

			logger.debug("API_URL_TXT" + interfaceDefinitionObj.getString(API_URL_TXT));
			
			String finalEndpointURL = urlMapper.updateEndPointURL(requestMap,
					interfaceDefinitionObj.getString(API_URL_TXT));
			logger.warn("final API Endpoint URL = " + finalEndpointURL);

			// Setting the API Headers
			JSONArray headerParams = jsonAdapterUtil.obtainJSONHeaderParams(interfaceJsonContent, requestMap);
			logger.debug("Final API Header Parameter = " + headerParams);
			logger.info("Final API Header Parameter = " + headerParams);
			logger.warn("Final API Header Parameter = " + headerParams);

			org.codehaus.jettison.json.JSONObject finalAPIRequest = requestMapper
					.requestParsingLogic(interfaceDefinitionObj, request, requestMap);
			logger.warn("Final API REQUEST::" + finalAPIRequest);

			return this.invokeExternalRestService(finalAPIRequest.toString(), headerParams,
					interfaceDefinitionObj.getString("methodType"), interfaceDefinitionObj.getString("contentType"),
					finalEndpointURL, new JSONObject(interfaceJsonContent), header, true, interfaceName, request);

		} catch (JSONException e) {
			logger.error(ERROR_CONST, e);
			return adapterUtil.setErrorMono("Request/Response parameters are missing.", "5");
		} catch (Exception e) {
			logger.error(ERROR_CONST, e);
			return adapterUtil.setErrorMono("Error in interface configuration", "1");
		}
	}

	private void getUpdatedJSONInterfaceURL(org.codehaus.jettison.json.JSONObject interfaceDefinitionObj,
			String interfaceName) {
		try {
			String apiURL = interfaceDefinitionObj.getString(API_URL_TXT);
			String urlStr = urlUtils.readExternalServiceURL(interfaceName);
			apiURL = apiURL.replace("${URL}", urlStr);
			interfaceDefinitionObj.put(API_URL_TXT, apiURL);
		} catch (Exception e) {
			logger.error(ERROR_CONST + e);
		}
	}

	private void getUpdatedDefaulInterfaceURL(String interfaceName, JSONObject interfaceJsonContent) {
		try {
			String apiURL = interfaceJsonContent.getString("endpointurl");
			String urlStr = urlUtils.readExternalServiceURL(interfaceName);
			apiURL = apiURL.replace("${URL}", urlStr);
			interfaceJsonContent.put("endpointurl", apiURL);
		} catch (Exception e) {
			logger.error(ERROR_CONST + e);
		}
	}
}
