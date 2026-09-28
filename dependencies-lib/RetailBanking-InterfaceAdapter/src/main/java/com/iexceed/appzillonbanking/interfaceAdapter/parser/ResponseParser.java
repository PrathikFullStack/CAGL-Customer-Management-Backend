package com.iexceed.appzillonbanking.interfaceAdapter.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ResponseParser {

	private static final Logger logger = LogManager.getLogger(ResponseParser.class);

	public String parseAPIResponse(String apiResponse, String interfaceName) {
		logger.debug("Start parseAPIResponse::");
		logger.debug("InterfaceName:" + interfaceName);
		logger.debug("Incoming Response:" + apiResponse);
		logger.debug("Final Response from parseAPIResponse::" + apiResponse);
		// Write your custom code logic to handle API responses based on the interface name.
		return apiResponse;
	}
}
