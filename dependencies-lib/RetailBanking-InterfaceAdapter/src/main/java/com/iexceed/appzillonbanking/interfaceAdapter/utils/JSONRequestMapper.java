package com.iexceed.appzillonbanking.interfaceAdapter.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.iexceed.appzillonbanking.core.utils.BaseDynamicValue;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.core.utils.CustomDynamicValue;

@Component
public class JSONRequestMapper {

	private static final Logger logger = LogManager.getLogger(JSONRequestMapper.class);

	/**
	 * Method to parse the request and generate new Request based on the request
	 * Definition.
	 * 
	 * @param externalIntfJSON External Interface Definition JSON
	 * @param apiRequest       API Request
	 * @param requestMap       APIRequest as a {@code Map<String, Object>}
	 * @return Returns a JSONObject containing the parsed fields.
	 * @throws JSONException
	 */
	public JSONObject requestParsingLogic(JSONObject externalIntfJSON, String apiRequest,
			Map<String, Object> requestMap) throws JSONException {

		logger.debug("Start::requestParsingLogic::RequestParameters=" + externalIntfJSON + ", apiRequest="
				+ apiRequest + ", requestMap=" + requestMap);
		JSONObject externalAPIJSON = new JSONObject();
		String externalAPIRequestDefinition = externalIntfJSON.get("requestDef").toString();
		// If Request Mapping is not defined return back the APIRequest as is.
		if (null == externalAPIRequestDefinition || "".equalsIgnoreCase(externalAPIRequestDefinition)) {
			JSONObject requestAPI = new JSONObject(apiRequest).getJSONObject("apiRequest");
			return requestAPI.getJSONObject("requestObj");
		}
		Map<String, Object> externalAPIReqMap = JsonFlattener.flattenAsMap(externalAPIRequestDefinition);
		logger.debug("External Request Defnition MAP::" + externalAPIReqMap);
		LinkedHashMap<String, Object> updatedExternalRequestMap = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : externalAPIReqMap.entrySet()) {
			String key = entry.getKey();
			String value = (String) entry.getValue();
			logger.debug("Key = " + key + ", Value = " + value);
			if (key.contains("~@JSONARRAY@")) {
				/**
				 * For Level1 JSONArray or JSONArray inside a Nested Objects - ~@JSONARRAY@
				 * denotes that the field needs to be consider for dynamic JSON String
				 * generation
				 */
				updateKeysInJSONArray(requestMap, key, value, updatedExternalRequestMap);
			} else {
				// For Level1 Elements and Nested JSONObjects, Match and replace the content
				String[] valueFields = value.substring(value.indexOf("{") + 1, value.indexOf("}")).split("~");

				String readValueFrom = valueFields[0].toUpperCase();
				String valueMapperKey = valueFields[1];

				switch (readValueFrom) {
				case "REQUEST":
					updatedExternalRequestMap.put(entry.getKey(), requestMap.get(valueMapperKey));
					break;
				case "PROPERTYFILE":
					String propertyValue = CommonUtils.getExternalProperties(valueMapperKey);
					updatedExternalRequestMap.put(entry.getKey(), propertyValue);
					break;
				case "CONST":
					updatedExternalRequestMap.put(entry.getKey(), valueMapperKey);
					break;
				case "BASEJAVACODE":
					BaseDynamicValue baseDynamicValue = new BaseDynamicValue();
					updatedExternalRequestMap.put(entry.getKey(), baseDynamicValue.generateValue(valueMapperKey));
					break;
				case "CUSTOMJAVACODE":
					CustomDynamicValue customDynamicValue = new CustomDynamicValue();
					updatedExternalRequestMap.put(entry.getKey(), customDynamicValue.generateValue(valueMapperKey));
					break;
				default:
					break;
				}
			}
		}
		logger.debug("Updated External Request MAP::" + updatedExternalRequestMap);
		// UnFlatten back the Request Map and convert to JSONObject.
		String unflattenedStr = JsonUnflattener.unflatten(updatedExternalRequestMap);
		unflattenedStr = unflattenedStr.replaceAll("\\\\\"", "");
		externalAPIJSON = new JSONObject(unflattenedStr);
		logger.debug("End::requestParsingLogic::Final Request JSON=" + new JSONObject(unflattenedStr));
		return externalAPIJSON;

	}

	private static void updateKeysInJSONArray(Map<String, Object> requestMap, String key, String value,
			LinkedHashMap<String, Object> updatedExternalRequestMap) throws JSONException {
		String[] splitArrKey = key.split("~@JSONARRAY@");
		String actualReqNodeName = splitArrKey[0];
		// Taking being index as 4 because the arrayKeySplit[1] starts with [0] -> 3 + 1
		String requiredArrayObjKeyName = splitArrKey[1].substring(4, splitArrKey[1].length());
		String[] valueMapperFields = value.substring(value.indexOf("{") + 1, value.indexOf("}")).split("~");

		/**
		 * Possible values for the field <b>readValueFrom<b> are 1. REQUEST -> Read
		 * value from the request 2. PROPERTYFILE -> Read value from the external
		 * property file 3. CONST -> Read the hard-coded value
		 */
		String readValueFrom = valueMapperFields[0];
		String valueMapperKey = valueMapperFields[1];
		if (readValueFrom.equalsIgnoreCase("REQUEST")) {
			String reqKeyNodeStruct = valueMapperKey.substring(0, valueMapperKey.lastIndexOf('.'));
			String exisitingArrayObjKeyName = valueMapperKey.substring(valueMapperKey.lastIndexOf('.') + 1);
			if (updatedExternalRequestMap.containsKey(actualReqNodeName)) {
				String exisitingJSONStr = updatedExternalRequestMap.get(actualReqNodeName).toString();
				String replacedJSON = exisitingJSONStr.replaceAll(exisitingArrayObjKeyName, requiredArrayObjKeyName);
				updatedExternalRequestMap.put(actualReqNodeName, new JSONArray(replacedJSON));

			} else {
				String exisitingJSONStr = requestMap.get(reqKeyNodeStruct).toString();
				String replacedJSON = exisitingJSONStr.replaceAll(exisitingArrayObjKeyName, requiredArrayObjKeyName);
				updatedExternalRequestMap.put(actualReqNodeName, new JSONArray(replacedJSON));
			}
		}
	}

}
