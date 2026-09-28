package com.iexceed.appzillonbanking.interfaceAdapter.service;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.core.constants.CommonConstants;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.ParserUtils;

import reactor.core.publisher.Mono;

@Component
public class InterfaceAdapter {

	@Autowired
	private HttpInterfaceParser httpInterfaceParser;

	/*
	 * @Autowired private SoapInterfaceParser soapInterfaceParser;
	 */
	@Autowired
	private AdapterUtil adapterUtil;

	@Autowired
	private ParserUtils parserUtils;

	private static final Logger logger = LogManager.getLogger(InterfaceAdapter.class);

	private static final String INTF_FILENOTFOUND_LG = "Please upload the interface file in the configured path.";

	private static final String REQ_RES_MISMATCH_LG = "Request/Response parameters are missing.";

	/**
	 * Method to call External Rest API by reading the content of external Interface
	 * file.
	 * 
	 * @param header         Request Header Parameters
	 * @param restApiRequest Front-End API request as {@code Object}
	 * @param interfaceName  Name of the External Interface File.
	 * @return Returns the API Response as {@code Mono<Object>}
	 */
	public Mono<Object> callExternalService(Header header, Object restApiRequest, String interfaceName) {

		logger.debug("Start : interface adapter flow=",
				restApiRequest + ", interfaceFileName=" + interfaceName + ", header=" + header);
		logger.debug("Printing restApiRequest callExternalService : {}", restApiRequest);
		logger.error("Printing restApiRequest callExternalService : {}", restApiRequest);
		logger.warn("Printing restApiRequest callExternalService : {}", restApiRequest);
		Mono<Object> apiResponseMono = Mono.just(new JSONObject());

		try {
			ObjectMapper mapperObj = new ObjectMapper();
			JSONObject requestWrapper = new JSONObject(mapperObj.writeValueAsString(restApiRequest));
			logger.debug("Printing requestWrapper callExternalService : {}", requestWrapper);
			logger.error("Printing requestWrapper callExternalService : {}", requestWrapper);
			logger.warn("Printing requestWrapper callExternalService : {}", requestWrapper);
		
			JSONObject request = new JSONObject();
			request.put(CommonConstants.API_REQUEST, requestWrapper);
			String interfaceFileName = interfaceName + ".apzinterface";

			String interfaceFileContent = parserUtils.readInterfaceContentFromServer(interfaceFileName);
			logger.debug("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			logger.error("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			logger.warn("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			JSONObject interfaceJsonContent = new JSONObject(interfaceFileContent);
			if ("HTTP".equalsIgnoreCase(interfaceJsonContent.get("servicetype").toString())) {
				logger.debug("Service type is http");
				apiResponseMono = httpInterfaceParser.callService(request, interfaceJsonContent, header, interfaceName);
			} else {
				logger.debug("Invalid Service Type");
				apiResponseMono = adapterUtil
						.setErrorMono("Invalid Service Type. Please configure the interface type as HTTP", "3");
			}
			logger.debug("Printing apiResponseMono callExternalService : {}", apiResponseMono);
			logger.error("Printing apiResponseMono callExternalService : {}", apiResponseMono);
			logger.warn("Printing apiResponseMono callExternalService : {}", apiResponseMono);
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while calling the service file not found, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono(INTF_FILENOTFOUND_LG, "2");
		} catch (Exception e) {
			logger.error("Error occurred while calling the service, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono(REQ_RES_MISMATCH_LG, "5");
		}
		logger.debug("Printing final apiResponseMono callExternalService : {}", apiResponseMono);
		logger.error("Printing final apiResponseMono callExternalService : {}", apiResponseMono);
		logger.warn("Printing final apiResponseMono callExternalService : {}", apiResponseMono);
		return apiResponseMono;
	}

	/**
	 * Method to call External SOAP API by reading the content of external Interface
	 * file.
	 * 
	 * @param header         Request Header Parameters
	 * @param restApiRequest Front-End API request as {@code Object}
	 * @param interfaceName  Name of the External Interface File.
	 * @return Returns the API Response as {@code Mono<Object>}
	 */
	/*
	 * public JSONObject callExternalSOAPService(Header header, Object
	 * restApiRequest, String interfaceName) {
	 * 
	 * logger.debug("Start : interface adapter flow=", restApiRequest +
	 * ", interfaceFileName=" + interfaceName + ", header=" + header); JSONObject
	 * apiResponse = new JSONObject(); try { ObjectMapper mapperObj = new
	 * ObjectMapper(); JSONObject requestWrapper = new
	 * JSONObject(mapperObj.writeValueAsString(restApiRequest));
	 * 
	 * JSONObject request = new JSONObject();
	 * request.put(CommonConstants.API_REQUEST, requestWrapper); String
	 * interfaceFileName = interfaceName + ".apzinterface";
	 * 
	 * String interfaceFileContent =
	 * parserUtils.readInterfaceContentFromServer(interfaceFileName); JSONObject
	 * interfaceJsonContent = new JSONObject(interfaceFileContent); if
	 * ("SOAP".equalsIgnoreCase(interfaceJsonContent.get("servicetype").toString()))
	 * { logger.debug("Service type is soap"); apiResponse =
	 * soapInterfaceParser.callService(request, interfaceJsonContent, header,
	 * interfaceName); } else { logger.debug("Invalid Service Type"); apiResponse =
	 * adapterUtil.
	 * setError("Invalid Service Type. Please configure the interface type as SOAP",
	 * "3"); } } catch (FileNotFoundException e) { logger.
	 * error("Error occurred while calling the service file not found, error = ",
	 * e); apiResponse = adapterUtil.setError(INTF_FILENOTFOUND_LG, "2"); } catch
	 * (Exception e) {
	 * logger.error("Error occurred while calling the service, error = ", e);
	 * apiResponse = adapterUtil.setError(REQ_RES_MISMATCH_LG, "5"); }
	 * logger.debug("End : adapter flow with response = ", apiResponse); return
	 * apiResponse;
	 * 
	 * }
	 */

	/**
	 * Method to call External Rest API by reading the content of external Interface
	 * file to process workflow.
	 * 
	 * @param interfaceRequest Front-End API request as {@code Object}
	 * @param interfaceName    Name of the External Interface File.
	 * @param header           Request Header Parameters
	 * @return Returns the API Response as {@code Mono<Object>}
	 */
	public Mono<Object> executeWorkflowAdapter(JSONObject interfaceRequest, String interfaceName, Header header) {
		logger.debug("Start : interface adapter flow");
		logger.debug("Printing interfaceRequest executeWorkflowAdapter : {}", interfaceRequest);
		logger.error("Printing interfaceRequest executeWorkflowAdapter : {}", interfaceRequest);
		logger.warn("Printing interfaceRequest executeWorkflowAdapter: {}", interfaceRequest);
		JSONObject apiResponse = new JSONObject();
		Mono<Object> apiResponseMono = Mono.just(new JSONObject());
		AdapterUtil adapterUtil = new AdapterUtil();
		try {
			logger.debug("Start : interface adapter request = " + interfaceRequest);
			String interfaceFileName = interfaceName + ".apzinterface";
			logger.debug("interfaceFileName =" + interfaceFileName);
			String interfaceFileContent = parserUtils.readInterfaceContentFromServer(interfaceFileName);
			logger.debug("interfaceFileContent = " + interfaceFileContent);
			
			logger.debug("Printing interfaceFileContent interfaceFileContent : {}", interfaceFileContent);
			logger.error("Printing interfaceFileContent interfaceFileContent : {}", interfaceFileContent);
			logger.warn("Printing interfaceFileContent interfaceFileContent: {}", interfaceFileContent);
			
			JSONObject interfaceJsonContent = new JSONObject(interfaceFileContent);
			String serviceType = interfaceJsonContent.get("servicetype").toString();
			if (serviceType.equalsIgnoreCase("HTTP")) {
				logger.debug("Service type is http");
				apiResponseMono = this.httpInterfaceParser.callService(interfaceRequest, interfaceJsonContent, header,
						interfaceName);
			} else if (serviceType.equalsIgnoreCase("SOAP")) {
				logger.debug("Service type is soap");
				/*
				 * apiResponse = this.soapInterfaceParser.callService(interfaceRequest,
				 * interfaceJsonContent, header, interfaceName);
				 */
			} else {
				logger.debug("Invalid Service Type");
				apiResponseMono = adapterUtil
						.setErrorMono("Invalid Service Type. Please configure the interface type as HTTP/SOAP", "3");
			}
			logger.debug("Printing apiResponseMono executeWorkflowAdapter : {}", apiResponseMono);
			logger.error("Printing apiResponseMono executeWorkflowAdapter : {}", apiResponseMono);
			logger.warn("Printing apiResponseMono executeWorkflowAdapter: {}", apiResponseMono);
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while calling the service file not found, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono("Please upload the interface file in the configured path.", "2");
		} catch (JSONException e) {
			logger.error("Error occurred while calling the service, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono("Request/Response parameters are missing.", "5");
		} catch (Exception e) {
			logger.error("Error occurred while calling the service, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono("Request/Response parameters are missing.", "5");

		}
		logger.debug("End : adapter flow with response = " + apiResponse);
		logger.debug("Printing final apiResponseMono executeWorkflowAdapter : {}", apiResponseMono);
		logger.error("Printing final apiResponseMono executeWorkflowAdapter : {}", apiResponseMono);
		logger.warn("Printing final apiResponseMono executeWorkflowAdapter: {}", apiResponseMono);
		return apiResponseMono;
	}

	/**
	 * Method to call External Rest API by reading the content of external Interface
	 * JSON definition file.
	 * 
	 * @param header             Request Header Parameters
	 * @param restApiRequest     Front-End API request as {@code Object}
	 * @param interfaceName      Name of the External JSON File.
	 * @param isExternalJSONCall Flag to denote that interface call will be via JSON
	 *                           definition
	 * @return Returns the API Response as {@code Mono<Object>}
	 */
	public Mono<Object> callExternalService(Header header, Object restApiRequest, String interfaceName,
			boolean isExternalJSONCall) {
		logger.warn("Start : interface adapter flow=",
				restApiRequest + ", interfaceFileName=" + interfaceName + ", header=" + header);
		
		logger.debug("Printing  restApiRequest callExternalService : {}", restApiRequest);
		logger.error("Printing  restApiRequest callExternalService : {}", restApiRequest);
		logger.warn("Printing  restApiRequest callExternalService: {}", restApiRequest);
		
		Mono<Object> apiResponseMono = Mono.just(new JSONObject());

		try {
			ObjectMapper mapperObj = new ObjectMapper();
			JSONObject requestWrapper = new JSONObject(mapperObj.writeValueAsString(restApiRequest));
			logger.debug("Printing requestWrapper : {}", requestWrapper);
			logger.error("Printing requestWrapper : {}", requestWrapper);
			
			logger.debug("Printing  requestWrapper callExternalService : {}", requestWrapper);
			logger.error("Printing  requestWrapper callExternalService : {}", requestWrapper);
			logger.warn("Printing  requestWrapper callExternalService: {}", requestWrapper);

			JSONObject request = new JSONObject();
			request.put(CommonConstants.API_REQUEST, requestWrapper);

			String interfaceFileName = interfaceName + ".json";

			String interfaceFileContent = parserUtils.readInterfaceJSONFileContentFromServer(interfaceFileName);
			logger.debug("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			logger.error("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			logger.warn("Printing interfaceFileContent callExternalService : {}", interfaceFileContent);
			JSONObject interfaceJsonContent = new JSONObject(interfaceFileContent);
			if ("HTTP".equalsIgnoreCase(interfaceJsonContent.get("servicetype").toString())) {
				logger.debug("Service type is http");
				apiResponseMono = httpInterfaceParser.callRestAPIService(request.toString(), interfaceFileContent,
						header, interfaceName);
			} else {
				logger.debug("Invalid Service Type");
				apiResponseMono = adapterUtil
						.setErrorMono("Invalid Service Type. Please configure the interface type as HTTP/SOAP", "3");
			}
			logger.debug("Printing apiResponseMono callExternalService : {}", apiResponseMono);
			logger.error("Printing apiResponseMono callExternalService : {}", apiResponseMono);
			logger.warn("Printing apiResponseMono callExternalService : {}", apiResponseMono);
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while calling the service file not found, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono(INTF_FILENOTFOUND_LG, "2");
		} catch (Exception e) {
			logger.error("Error occurred while calling the service, error = ", e);
			apiResponseMono = adapterUtil.setErrorMono(REQ_RES_MISMATCH_LG, "5");
		}
		logger.debug("Printing final callExternalService apiResponseMono : {}", apiResponseMono);
		logger.error("Printing final  callExternalService apiResponseMono : {}", apiResponseMono);
		logger.warn("Printing final  callExternalService apiResponseMono : {}", apiResponseMono);
		return apiResponseMono;

	}
}
