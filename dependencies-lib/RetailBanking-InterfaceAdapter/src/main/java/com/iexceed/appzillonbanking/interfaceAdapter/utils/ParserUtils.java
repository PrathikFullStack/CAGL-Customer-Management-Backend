package com.iexceed.appzillonbanking.interfaceAdapter.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.iexceed.appzillonbanking.core.utils.CommonUtils;

@Component
public class ParserUtils {

	private static final Logger logger = LogManager.getLogger(ParserUtils.class);

	private LinkedHashMap<String, ArrayList<String>> hmap = new LinkedHashMap<>();
	private ArrayList<JSONObject> elementsList = new ArrayList<>();

	public void reinitializeGlobalVariables() {
		logger.debug("Start : reinitializeGlobalVariables with counter");
		hmap = new LinkedHashMap<>();
		elementsList = new ArrayList<>();
		logger.debug("End : reinitializeGlobalVariables");
	}

	/**
	 * Method to read the interface file content from external file Path.
	 * 
	 * @param fileName
	 * @return Returns the Interface file contents as String data.
	 * @throws IOException
	 */
	public String readInterfaceContentFromServer(String fileName) throws IOException {

		if (!fileName.contains("apzinterface")) {
			fileName = fileName.concat(".apzinterface");
		}
		String serverFilePath = CommonUtils.getExternalProperties("interFaceDir") + fileName;
		logger.debug("Server File Path in readInterfaceContentFromServer::" + serverFilePath);
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
			logger.error(e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		logger.warn("Interface file Content::" + serverFilePath);
		return sb.toString();

	}

	/**
	 * Method to read the interface JSON file content from external file Path.
	 * 
	 * @param fileName
	 * @return Returns the Interface JSON file contents as String data.
	 * @throws IOException
	 */
	public String readInterfaceJSONFileContentFromServer(String fileName) throws IOException {

		if (!fileName.contains("json")) {
			fileName = fileName.concat(".json");
		}
		String serverFilePath = CommonUtils.getExternalProperties("interFaceJSONDir") + fileName;
		logger.warn("Server File Path in readInterfaceJSONFileContentFromServer=" + serverFilePath);
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
			logger.error(e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		logger.warn("Interface file Content::" + serverFilePath);
		return sb.toString();
	}

	/**
	 * Calling obtainInterfaceNodes method to obtain a JSONObject containing the
	 * details of REQUESTDATAMODEL Input : JSONObject containing the interface
	 * details Output: JSONObject containing the REQUESTDATAMODEL details
	 * 
	 * @param fileJson
	 * @return
	 */
	public JSONObject obtainRequestObject(JSONObject fileJson) {
		JSONObject reqJson = new JSONObject();
		JSONArray reqJsonArray;
		Iterator<String> keys = fileJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (fileJson.get(key) instanceof JSONArray) {
				reqJsonArray = fileJson.getJSONArray(key);
				for (int i = 0; i < reqJsonArray.length(); i++) {
					reqJson = (JSONObject) reqJsonArray.get(i);
					String modelType = reqJson.get("typeclass").toString();
					if (modelType.equalsIgnoreCase("REQUESTDATAMODEL")) {
						return reqJson;
					}
				}
			}
		}
		return reqJson;
	}

	/**
	 * Calling obtainInterfaceNodes method to obtain a map containing all the parent
	 * and child nodes Input : JSONObject containing the interface details Output:
	 * Map containing all the parent and child nodes
	 * 
	 * @param fileJson
	 * @return List of Interface Nodes
	 */
	public Map<String, ArrayList<String>> obtainInterfaceNodes(JSONObject fileJson) {

		JSONObject reqJson;
		JSONArray reqJsonArray;
		Iterator<String> keys = fileJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (fileJson.get(key) instanceof JSONArray) {
				reqJsonArray = fileJson.getJSONArray(key);
				for (int i = 0; i < reqJsonArray.length(); i++) {
					reqJson = (JSONObject) reqJsonArray.get(i);
					if (reqJson.has(key)) {
						String type = reqJson.get("type").toString();
						if (type.equalsIgnoreCase("node")) {

							JSONArray tempJsonArray = (JSONArray) reqJson.get(key);
							JSONObject tempJson = new JSONObject();
							String parentNode = reqJson.has("alias")
									? (reqJson.get("alias").toString() + ":" + reqJson.get("name").toString())
									: (reqJson.get("name").toString());
							ArrayList<String> childNodeList = new ArrayList<String>();
							for (int j = 0; j < tempJsonArray.length(); j++) {
								tempJson = (JSONObject) tempJsonArray.get(j);
								if ((tempJson.get("type").toString()).equalsIgnoreCase("node")) {
									if (tempJson.has("alias")) {
										childNodeList.add(tempJson.get("alias").toString() + ":"
												+ tempJson.get("name").toString());
									} else {
										childNodeList.add(tempJson.get("name").toString());
									}
								}
							}
							hmap.put(parentNode, childNodeList);
						}
						// Using recursion to parse the entire JSON object
						obtainInterfaceNodes(reqJson);
					}
				}
			}
		}
		return hmap;
	}

	/**
	 * Calling obtainInterfaceElements method to obtain a JSON containing all the
	 * elements of the interface Input : JSONObject containing the interface details
	 * Output: ArrayList containing all the elements of the interface
	 * 
	 * @param fileJson
	 * @return List of interface elements
	 * @throws JSONException
	 */
	public List<JSONObject> obtainInterfaceElements(JSONObject fileJson) throws JSONException {

		JSONObject reqJson;
		JSONArray reqJsonArray;
		Iterator<String> keys = fileJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (fileJson.get(key) instanceof JSONArray) {
				reqJsonArray = fileJson.getJSONArray(key);
				for (int i = 0; i < reqJsonArray.length(); i++) {
					reqJson = (JSONObject) reqJsonArray.get(i);
					// Using recursion to parse the entire JSON object
					if (reqJson.has(key) && (!((reqJson.get("type")).toString().equalsIgnoreCase("element")))) {
						obtainInterfaceElements(reqJson);
					}
					if ((reqJson.get("type")).toString().equalsIgnoreCase("element")) {
						elementsList.add(reqJson);
					}
				}
			}
		}
		return elementsList;
	}

	public JSONObject formSimpleJson(JSONObject finalJson, String nodeName, String keyName, String value,
			JSONObject outputJson) {

		if (finalJson.has(nodeName)) {
			String currentContents = finalJson.get(nodeName).toString();
			finalJson.remove(nodeName);
			JSONArray jsonArr = new JSONArray();
			// support for json arrays
			/*
			 * if ((value != null) && (value.startsWith("[{")) && (value.endsWith("}]"))) {
			 * jsonArr = new JSONArray(value); JSONObject completeJson = new
			 * JSONObject(currentContents); completeJson.put(keyName, jsonArr);
			 * finalJson.put(nodeName, completeJson); }
			 * 
			 * // support for integer/string arrays else if ((value != null) &&
			 * (value.startsWith("[")) && (value.endsWith("]"))) {
			 */
			if ((value != null) && (value.startsWith("[")) && (value.endsWith("]"))) {
				value = value.replace("[", "").replace("]", "");
				Object[] arrContents = value.split(",");

				for (Object str : arrContents) {
					jsonArr.put(str);
				}
				JSONObject completeJson = new JSONObject(currentContents);
				completeJson.put(keyName, jsonArr);
				finalJson.put(nodeName, completeJson);
			} else {
				JSONObject completeJson = new JSONObject(currentContents);
				completeJson.put(keyName, value);
				finalJson.put(nodeName, completeJson);
			}
		} else {
			if (outputJson.length() == 0) {
				JSONObject tempJson = new JSONObject();
				tempJson.put(keyName, value);
				finalJson.put(nodeName, tempJson);
			} else {
				finalJson.put(nodeName, outputJson);
			}
		}
		return finalJson;
	}

	/**
	 * Calling obtainNestedJson method to obtain a nested JSON containing all the
	 * nodes and its elements in the required hierarchical structure Input :
	 * JSONObject object obtained from the method "obtainSimpleJson" and the map
	 * containing the details of parent and child nodes Output: JSONObject
	 * containing all the nodes and its elements in the required hierarchical
	 * structure
	 * 
	 * @param simpleJson
	 * @param nodeMap
	 * @return JSONObject
	 * @throws JSONException
	 */
	public JSONObject obtainNestedJson(JSONObject simpleJson, Map<String, ArrayList<String>> nodeMap)
			throws JSONException {

		JSONObject finalNestedJson = new JSONObject();
		LinkedHashMap<String, ArrayList<String>> reversedMap = new LinkedHashMap<>();

		List<String> alKeys = new ArrayList<>(nodeMap.keySet());
		Collections.reverse(alKeys);

		for (String strKey : alKeys) {
			reversedMap.put(strKey, nodeMap.get(strKey));
		}
		for (Map.Entry<String, ArrayList<String>> mapData : reversedMap.entrySet()) {
			String parentNodeName = mapData.getKey();
			ArrayList<String> childValues = mapData.getValue();

			if (childValues.toString().equals("[]")) {
				if (simpleJson.has(parentNodeName)) {
					finalNestedJson.put(parentNodeName, simpleJson.get(parentNodeName));
				}
			} else {
				for (String childNodeName : childValues) {
					if (simpleJson.has(childNodeName)) {
						if (finalNestedJson.has(childNodeName)) {
							String childNodeContents = finalNestedJson.get(childNodeName).toString();
							JSONObject childJson = new JSONObject(childNodeContents);
							if (finalNestedJson.has(childNodeName)) {
								JSONObject parentJson = new JSONObject();

								if (simpleJson.has(parentNodeName)) {
									if (finalNestedJson.has(parentNodeName)) {
										finalNestedJson.remove(childNodeName);
										parentJson = finalNestedJson.getJSONObject(parentNodeName);
										parentJson.put(childNodeName, childJson);
										finalNestedJson.put(parentNodeName, parentJson);
									} else {
										finalNestedJson.remove(childNodeName);
										parentJson = simpleJson.getJSONObject(parentNodeName);
										parentJson.put(childNodeName, childJson);
										finalNestedJson.put(parentNodeName, parentJson);
									}
								} else {
									finalNestedJson.remove(childNodeName);

									if (finalNestedJson.has(parentNodeName)) {
										parentJson = finalNestedJson.getJSONObject(parentNodeName);
									}
									parentJson.put(childNodeName, childJson);
									finalNestedJson.put(parentNodeName, parentJson);
								}
							}
						} else {
							finalNestedJson.put(childNodeName, simpleJson.get(childNodeName));
						}
					} else {
						String currentChildContents = finalNestedJson.get(childNodeName).toString();

						if (finalNestedJson.has(parentNodeName)) {
							String currentParentContents = finalNestedJson.get(parentNodeName).toString();
							finalNestedJson.remove(childNodeName);
							JSONObject tempChildJson = new JSONObject(currentChildContents);
							JSONObject tempParentJson = new JSONObject(currentParentContents);
							tempParentJson.put(childNodeName, tempChildJson);
							finalNestedJson.put(parentNodeName, tempParentJson);
						} else {
							JSONObject tempChildJson = new JSONObject(currentChildContents);
							finalNestedJson.remove(childNodeName);
							JSONObject tempChildJson1 = new JSONObject();
							tempChildJson1.put(childNodeName, tempChildJson);
							finalNestedJson.put(parentNodeName, tempChildJson1);
						}
					}
				}
			}
		}
		return finalNestedJson;
	}

	public JSONObject formSimpleResponseJson(JSONObject finalJson, String nodeName, String keyName, String value,
			JSONObject outputJson) {

		if (finalJson.has(nodeName)) {
			String currentContents = finalJson.get(nodeName).toString();
			finalJson.remove(nodeName);
			// support for arrays
			if ((value != null) && (value.startsWith("[")) && (value.endsWith("]"))) {
				logger.debug("support for arrays");
				JSONArray newArr = new JSONArray(value);
				JSONArray combinedArr = new JSONArray();
				JSONObject completeJson = null;

				if ((currentContents != null) && (currentContents.startsWith("[")) && (currentContents.endsWith("]"))) {
					JSONArray existingArray = new JSONArray(currentContents);

					for (int i = 0; i < existingArray.length(); i++) {
						JSONObject tempJson = existingArray.getJSONObject(i);
						tempJson.put(keyName, (i < newArr.length()) ? newArr.get(i) : "");
						combinedArr.put(tempJson);
					}
				} else {
					completeJson = new JSONObject(currentContents);
					Iterator<String> keys = completeJson.keys();
					JSONArray existingArray;
					JSONObject existingJson = new JSONObject();
					while (keys.hasNext()) {
						String key = keys.next();
						// support for json arrays
						if (completeJson.get(key) instanceof JSONArray) {
							logger.debug("combining existing array with new array");
							existingArray = completeJson.getJSONArray(key);
							for (int i = 0; i < existingArray.length(); i++) {
								JSONObject tempJson = new JSONObject();
								tempJson.put(key, existingArray.get(i));
								tempJson.put(keyName, (i < newArr.length()) ? newArr.get(i) : "");
								combinedArr.put(tempJson);
							}
						} else { // support for json objects
							logger.debug("combining existing array with new object");
							existingJson.put(key, completeJson.get(key).toString());
							combinedArr.put(existingJson);
						}
					}
				}
				finalJson.put(nodeName, combinedArr);
			} else {
				JSONObject completeJson = new JSONObject(currentContents);
				completeJson.put(keyName, value);
				finalJson.put(nodeName, completeJson);
			}
		} else {
			if (outputJson.length() == 0) {
				JSONObject tempJson = new JSONObject();
				tempJson.put(keyName, value);
				finalJson.put(nodeName, tempJson);
			} else {
				finalJson.put(nodeName, outputJson);
			}
		}
		return finalJson;
	}

	/**
	 * Calling obtainInterfaceNodes method to obtain a JSONObject containing the
	 * details of RESPONSEDATAMODEL Input : JSONObject containing the interface
	 * details Output: JSONObject containing the RESPONSEDATAMODEL details
	 * 
	 * @param fileJson
	 * @return
	 */
	public JSONObject obtainResponseObject(JSONObject fileJson) {

		JSONObject respJson = new JSONObject();
		JSONArray reqJsonArray = null;

		Iterator<String> keys = fileJson.keys();
		while (keys.hasNext()) {
			String key = keys.next();

			if (fileJson.get(key) instanceof JSONArray) {
				reqJsonArray = fileJson.getJSONArray(key);
				for (int i = 0; i < reqJsonArray.length(); i++) {
					respJson = (JSONObject) reqJsonArray.get(i);
					String modelType = respJson.get("typeclass").toString();
					if (modelType.equalsIgnoreCase("RESPONSEDATAMODEL")) {
						return respJson;
					}
				}
			}
		}
		return respJson;
	}
}
