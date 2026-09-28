package com.iexceed.appzillonbanking.interfaceAdapter.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.iexceed.appzillonbanking.core.utils.CommonUtils;

@Component
public class URLUtils {

	private static final Logger logger = LogManager.getLogger(URLUtils.class);

	public String readExternalServiceURL(String interfaceName) throws IOException {

		String interfaceURL = "";
		JSONObject externalURLJSON = new JSONObject();
		String urlFilePath = CommonUtils.getExternalProperties("externalServiceURLPath");
		logger.warn("Server File Path in externalServiceURLPath=" + urlFilePath);
		BufferedReader br = new BufferedReader(new FileReader(urlFilePath));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		externalURLJSON = new JSONObject(sb.toString());
		logger.warn("Final External URL Map::" + urlFilePath);
		interfaceURL = externalURLJSON.get(interfaceName).toString();
		return interfaceURL;
	}
}
