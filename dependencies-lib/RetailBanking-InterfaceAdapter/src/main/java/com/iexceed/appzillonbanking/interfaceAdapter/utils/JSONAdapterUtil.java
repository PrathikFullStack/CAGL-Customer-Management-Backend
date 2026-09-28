package com.iexceed.appzillonbanking.interfaceAdapter.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.iexceed.appzillonbanking.core.utils.BaseDynamicValue;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.core.utils.CustomDynamicValue;

/**
 * JSON Adapter Utility Class
 * 
 * @author akshay.upadhya
 * @since 08.10.2022
 */
@Component
public class JSONAdapterUtil {

	private static final Logger logger = LogManager.getLogger(JSONAdapterUtil.class);

	public Map<String, Object> flattenJSONToMap(String json) {
		logger.debug("Start::flattenJSONToMap");
		Map<String, Object> map = new HashMap<>();
		try {
			constructTraversalPathForMap("", new ObjectMapper().readTree(json), map);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		logger.debug("End::flattenJSONToMap=", map);
		return map;
	}

	private void constructTraversalPathForMap(String currentPath, JsonNode jsonNode, Map<String, Object> map)
			throws JSONException {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";
			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				// If Empty Object needs to be added as part of the request
				if ("{}".equalsIgnoreCase(entry.getValue().toString())) {
					map.put(pathPrefix + entry.getKey(), new JSONObject());
				} else {
					constructTraversalPathForMap(pathPrefix + entry.getKey(), entry.getValue(), map);
				}
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			updateMapBasedOnArrayNode(arrayNode.toString(), map, currentPath);
		} else if (jsonNode.isValueNode()) {
			ValueNode valueNode = (ValueNode) jsonNode;
			updateMapBasedOnValueNode(valueNode, map, currentPath);
		}
	}

	private void updateMapBasedOnArrayNode(String arrStr, Map<String, Object> map, String currentPath)
			throws JSONException {
		if (arrStr.startsWith("[]")) {
			// Empty String[]
			map.put(currentPath + "[]", new ArrayList<>());
		} else if (arrStr.startsWith("[{") && arrStr.endsWith("}]")) {
			// JSONArray
			map.put(currentPath + "[]", new JSONArray(arrStr));
		} else if (arrStr.startsWith("[") && arrStr.endsWith("]")) {
			// List<Object> with string data
			String stringArrStr = arrStr.substring(1, arrStr.length() - 1);
			map.put(currentPath + "[]", Arrays.asList(stringArrStr.split(",")));
		}
	}

	/**
	 * Method to set the respective dataType of the request fields
	 * 
	 * @param valueNode
	 * @param map
	 * @param currentPath
	 */
	private void updateMapBasedOnValueNode(ValueNode valueNode, Map<String, Object> map, String currentPath) {
		if (valueNode.isInt()) {// Get value as an integer
			map.put(currentPath, valueNode.asInt());
		} else if (valueNode.isDouble()) { // Get value as Double
			map.put(currentPath, valueNode.asDouble());
		} else if (valueNode.isLong()) { // Get value as Long
			map.put(currentPath, valueNode.asLong());
		} else if (valueNode.isBoolean()) { // Get value as boolean
			map.put(currentPath, valueNode.asBoolean());
		} else if (valueNode.isTextual()) { // Get value as String
			map.put(currentPath, valueNode.asText());
		} else if (valueNode.isArray()) { // Get value as Array
			map.put(currentPath, valueNode.asText());
		}
	}

	/**
	 * Method to read the interface JSON file content from external file Path.
	 * 
	 * @author akshay.upadhya
	 * @since 08.10.2022
	 * @param fileName
	 * @return Returns the Interface JSON file contents as String data.
	 * @throws IOException
	 */
	public String readInterfaceJSONFileContentFromServer(String fileName) throws IOException {

		if (!fileName.contains("json")) {
			fileName = fileName.concat(".json");
		}
		String serverFilePath = CommonUtils.getCommonProperties("interFaceJSONDir") + fileName;
		logger.warn("Server File Path in readInterfaceJSONFileContentFromServer=", serverFilePath);
		BufferedReader br = new BufferedReader(new FileReader(serverFilePath));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		logger.warn("Interface JSON FILE Content::", sb);
		return sb.toString();
	}

	/**
	 * Method to generate the Header Parameters based on the External API JSON
	 * Definition file.
	 * 
	 * @author akshay.upadhya
	 * @since 08.10.2022
	 * @param interfaceDefinitionStr External API JSON Definition file in
	 *                               {@code String} format
	 * @param requestMap
	 * @return Array of Header parameters as name value pairs.
	 */
	public JSONArray obtainJSONHeaderParams(String interfaceDefinitionStr, Map<String, Object> requestMap) {

		logger.debug("Start obtainJSONHeaderParams");
		JSONArray headerArray = new JSONArray();
		try {
			JSONObject interfaceDefinitionObj = new JSONObject(interfaceDefinitionStr);
			if (interfaceDefinitionObj.has("headers")) {
				JSONObject headerParams = interfaceDefinitionObj.getJSONObject("headers");
				Iterator<String> headerIter = headerParams.keys();
				while (headerIter.hasNext()) {
					String key = headerIter.next();
					JSONObject headerObj = new JSONObject();
					String value = headerParams.get(key).toString();
					String headerVal = "";
					if (value.contains("~")) {
						String readVal = value.substring(1, value.length() - 1);
						String readValueFrom = readVal.split("~")[0].toUpperCase();
						String valueMapperKey = readVal.split("~")[1];
						switch (readValueFrom) {
						case "REQUEST":
							headerVal = requestMap.get(valueMapperKey).toString();
							break;
						case "PROPERTYFILE":
							headerVal = CommonUtils.getExternalProperties(valueMapperKey);
							break;
						case "CONST":
							headerVal = valueMapperKey;
							break;
						case "BASEJAVACODE":
							BaseDynamicValue baseDynamicValue = new BaseDynamicValue();
							headerVal = baseDynamicValue.generateValue(valueMapperKey);
							break;
						case "CUSTOMJAVACODE":
							CustomDynamicValue customDynamicValue = new CustomDynamicValue();
							headerVal = customDynamicValue.generateValue(valueMapperKey);
							break;
						default:
							headerVal = headerParams.get(key).toString();
							break;
						}
					} else {
						headerVal = value;
					}
					headerObj.put("name", key);
					headerObj.put("value", headerVal);
					headerArray.put(headerObj);
				}
			} else {
				logger.debug("Header parameters are not configured");
			}
		} catch (Exception e) {
			logger.error("Exception Occured:", e);
		}
		logger.debug("End obtainJSONHeaderParams=", headerArray);
		return headerArray;
	}
}
