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

@Component
public class JSONResponseMapper {

	private static final Logger logger = LogManager.getLogger(JSONResponseMapper.class);

	/**
	 * Method to parse the response and convert to the desired front end format.
	 * 
	 * @param externalIntfJSON External JSON Definition file content as
	 *                         {@code String}.
	 * @param extAPIResponse   External API Response as {@code String}.
	 * @param restApiResponse  Updated API Response as {@code JSONObject}
	 * @return Returns a JSONObject post mapping the data based on the JSON
	 *         Definition.
	 */
	public JSONObject responseParsingLogic(String externalIntfJSON, String extAPIResponse, JSONObject restApiResponse) {

		JSONAdapterUtil jsonAdapterUtil = new JSONAdapterUtil();
		JSONObject internalAPIJSON = new JSONObject();
		try {
			String internalResponseDefnition = new JSONObject(externalIntfJSON).get("responseDef").toString();

			if (null == internalResponseDefnition || "".equalsIgnoreCase(internalResponseDefnition)
					|| "[]".equalsIgnoreCase(internalResponseDefnition)) {
				return new JSONObject(extAPIResponse);
			}
			Map<String, Object> apiResponseMap = jsonAdapterUtil
					.flattenJSONToMap(restApiResponse.toString());
			logger.debug("External API RESPONSE MAP::" + apiResponseMap);

			Map<String, Object> internalResponseMap = JsonFlattener.flattenAsMap(internalResponseDefnition);
			logger.debug("API Response MAP::" + internalResponseMap);
			LinkedHashMap<String, Object> updatedInternalResponseMap = new LinkedHashMap<>();

			for (Map.Entry<String, Object> entry : internalResponseMap.entrySet()) {
				String key = entry.getKey();
				String value = (String) entry.getValue();
				logger.debug("Key = " + key + ", Value = " + value);
				String[] valueFields = value.substring(value.indexOf("{") + 1, value.indexOf("}")).split("~");
				if (valueFields[0].equalsIgnoreCase("RESPONSE")) {
					// Nested Array and Array Mapping
					if (valueFields[1].contains("[]")) {
						parseResponseForArrays(key, valueFields, apiResponseMap, updatedInternalResponseMap);
					} else { // Level1 field or Nested JSONObject
						if (apiResponseMap.containsKey(valueFields[1])) {
							updatedInternalResponseMap.put(key, apiResponseMap.get(valueFields[1]));
						} else {
							updatedInternalResponseMap.put(key, "");
						}
					}
				}
			}
			logger.debug("Updated External Request MAP::" + updatedInternalResponseMap);
			// UnFlatten back the Response Map and convert to JSONObject.
			String unflattenedStr = JsonUnflattener.unflatten(updatedInternalResponseMap);
			internalAPIJSON = new JSONObject(unflattenedStr);
			logger.debug("End::responseParsingLogic::Final Request JSON=" + internalAPIJSON);
		} catch (Exception e) {
			logger.error("Exception Occured::", e);
			try {
				return new JSONObject(extAPIResponse);
			} catch (JSONException e1) {
				logger.error("Inside Initial Catch Method:: Exception Occured::", e);
			}
		}
		return internalAPIJSON;
	}
	
	/**
	 * Method to parse the response for the Arrays.
	 * 
	 * @param key                        Response Mapper Key
	 * @param valueFields                Configured mapper value
	 * @param apiResponseMap             {@code Map} data of the apiResponse.
	 * @param updatedInternalResponseMap {@code LinkedHashMap} data of parsed
	 *                                   response Array.
	 */
	private void parseResponseForArrays(String key, String[] valueFields, Map<String, Object> apiResponseMap,
			LinkedHashMap<String, Object> updatedInternalResponseMap) {
		try {
			String externalResponseArrStr = apiResponseMap
					.get(valueFields[1].substring(0, valueFields[1].indexOf("]") + 1)).toString();

			String responseKey = valueFields[1].substring(valueFields[1].indexOf("]") + 2, valueFields[1].length());
			/**
			 * \\ is required for escaping '['
			 */
			String internalResponseNode = key.split("\\[")[0];
			String internalResponseKey = key.split("]")[1].toString().substring(1, key.split("]")[1].length());
			logger.debug("ExternAPIRESP::" + apiResponseMap);
			JSONArray externalResponseArray = new JSONArray(externalResponseArrStr);
			JSONArray responseArray = new JSONArray();
			// If node exists in the updated Interface response Map, Loop through the
			// existing node and append the new key values.
			if (updatedInternalResponseMap.containsKey(internalResponseNode)
					&& updatedInternalResponseMap.get(internalResponseNode) instanceof JSONArray) {
				responseArray = new JSONArray(updatedInternalResponseMap.get(internalResponseNode).toString());
				for (int i = 0; i < responseArray.length(); i++) {
					if (externalResponseArray.getJSONObject(i).has(responseKey)) {
						responseArray.getJSONObject(i).put(internalResponseKey,
								externalResponseArray.getJSONObject(i).get(responseKey));
					} else {
						responseArray.getJSONObject(i).put(internalResponseKey, "");
					}
				}
			} else {
				// If first execution of the key inside the array element
				for (int i = 0; i < externalResponseArray.length(); i++) {
					JSONObject respObj = new JSONObject();
					if (externalResponseArray.getJSONObject(i).has(responseKey)) {
						respObj.put(internalResponseKey, externalResponseArray.getJSONObject(i).get(responseKey));
					} else {
						respObj.put(internalResponseKey, "");
					}
					responseArray.put(respObj);
				}
			}
			updatedInternalResponseMap.put(internalResponseNode, responseArray);
		} catch (Exception e) {
			logger.error("Exception Occured::", e);
		}
	}
}
