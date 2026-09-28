/*
 * package com.iexceed.appzillonbanking.interfaceAdapter.service;
 * 
 * import java.io.FileNotFoundException; import java.time.LocalDateTime; import
 * java.util.ArrayList; import java.util.LinkedHashMap; import java.util.List;
 * import java.util.Map;
 * 
 * import org.apache.logging.log4j.LogManager; import
 * org.apache.logging.log4j.Logger; import org.json.JSONArray; import
 * org.json.JSONException; import org.json.JSONObject; import org.json.XML;
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.stereotype.Component;
 * 
 * import com.iexceed.appzillonbanking.core.constants.CommonConstants; import
 * com.iexceed.appzillonbanking.core.payload.Header; import
 * com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil; import
 * com.iexceed.appzillonbanking.interfaceAdapter.utils.ParserUtils; import
 * com.iexceed.appzillonbanking.interfaceAdapter.utils.URLUtils; import
 * com.iexceed.appzillonbanking.logs.service.LogExternalReqRes;
 * 
 * @Component public class SoapInterfaceParser {
 * 
 * @Autowired private SoapService soapService;
 * 
 * @Autowired private LogExternalReqRes logExternalReqRes;
 * 
 * @Autowired private AdapterUtil adapterUtil;
 * 
 * @Autowired private ParserUtils parserUtils;
 * 
 * @Autowired private URLUtils urlUtils;
 * 
 * private static final Logger logger =
 * LogManager.getLogger(SoapInterfaceParser.class);
 * 
 * public JSONObject callService(JSONObject request, JSONObject
 * interfaceJsonContent, Header header, String interfaceName) {
 * 
 * logger.debug("Start : soap callService with request = " +
 * request.toString()); JSONObject soapApiResponse;
 * 
 * LocalDateTime startDateTime = LocalDateTime.now();
 * 
 * JSONObject nestedJson = new JSONObject(); String soapOutput = null; String
 * status;
 * 
 * try {
 * 
 * JSONObject requiredObject =
 * parserUtils.obtainRequestObject(interfaceJsonContent);
 * logger.debug("requiredObject = " + requiredObject);
 * 
 * Map<String, ArrayList<String>> finalMap =
 * parserUtils.obtainInterfaceNodes(requiredObject);
 * 
 * LinkedHashMap<String, ArrayList<String>> elemestsMap =
 * soapService.obtainSoapInterfaceNodes(requiredObject);
 * 
 * finalMap.putAll(elemestsMap); logger.debug("finalMap = " + finalMap);
 * 
 * List<JSONObject> parsedList =
 * parserUtils.obtainInterfaceElements(requiredObject); JSONArray parsedArray =
 * new JSONArray(parsedList.toString()); logger.debug("parsedArray = " +
 * parsedArray);
 * 
 * JSONObject simpleJson = soapService.obtainSimpleSoapJson(request,
 * parsedArray); logger.debug("simpleJson = " + simpleJson);
 * 
 * nestedJson = parserUtils.obtainNestedJson(simpleJson, finalMap);
 * logger.debug("nestedJson = " + nestedJson);
 * 
 * String nestedXml = XML.toString(nestedJson); nestedXml =
 * SoapInterfaceParser.filterXml(nestedXml);
 * logger.debug("nestedXml post xml filter:" + nestedXml); String
 * ignoreNamespace =
 * interfaceJsonContent.get("soapignorenamespaces").toString();
 * logger.debug("ignoreNamespace = " + ignoreNamespace);
 * 
 * String namespace = interfaceJsonContent.get("soapnamespaces").toString();
 * logger.debug("namespace = " + namespace);
 * 
 * String soapXml = soapService.addNamespace(ignoreNamespace, namespace,
 * nestedXml); logger.debug("soapXml = " + soapXml);
 * 
 * String soapAction = "";
 * 
 *//**
	 * @author akshay.upadhya
	 * @since 05.07.21 Bug Id:- 52662 Bug fix to read the "soapaction" variable from
	 *        interfaceFile, Hence adding the toLowerCase()
	 *//*
		 * if (interfaceJsonContent.has(CommonConstants.SOAP_ACTION.toLowerCase())) {
		 * soapAction =
		 * interfaceJsonContent.get(CommonConstants.SOAP_ACTION.toLowerCase()).toString(
		 * ); }
		 * 
		 * String endpointurl = interfaceJsonContent.get("endpointurl").toString();
		 * this.getUpdatedDefaulInterfaceURL(interfaceName, interfaceJsonContent);
		 * logger.debug("endpointurl = " + endpointurl);
		 * 
		 * try { String xmlOutput = soapService.executeSoapApi(soapAction, soapXml,
		 * endpointurl); logger.debug("xmlOutput = " + xmlOutput);
		 * 
		 * String finalResponse = removeXmlStringNamespaceAndPreamble(xmlOutput);
		 * soapOutput = (XML.toJSONObject(finalResponse)).toString();
		 * 
		 * if ((soapOutput == null) || (soapOutput.trim().equals(""))) {
		 * logger.debug("empty response received"); soapApiResponse =
		 * adapterUtil.setError("Empty response received from the API", "7"); status =
		 * "1"; }
		 * 
		 * else { logger.debug("api execution is successful"); soapApiResponse =
		 * adapterUtil.setSuccessResp(soapOutput); status = "0"; } }
		 * 
		 * catch (Exception e) {
		 * logger.error("Error occurred while executing the soap api, error = " +
		 * e.getMessage()); soapApiResponse =
		 * adapterUtil.setError("SOAP API execution failed", "4"); status = "1"; } }
		 * 
		 * catch (FileNotFoundException e) {
		 * logger.error("Error occurred while executing the soap service, error = " +
		 * e.getMessage()); soapApiResponse = adapterUtil.
		 * setError("Please upload the interface file in the configured path.", "2");
		 * status = "1"; }
		 * 
		 * catch (JSONException e) {
		 * logger.error("Error occurred while executing the soap service, error = " +
		 * e.getMessage()); soapApiResponse =
		 * adapterUtil.setError("Request/Response parameters are missing.", "5"); status
		 * = "1"; }
		 * 
		 * catch (Exception e) {
		 * logger.error("Error occurred while executing the soap service, error = " +
		 * e.getMessage()); soapApiResponse = adapterUtil.setError(e.getMessage(), "1");
		 * status = "1"; }
		 * 
		 * LocalDateTime endDateTime = LocalDateTime.now();
		 * logger.debug("logging the request and response in db");
		 * 
		 * logExternalReqRes.logTransactionToDb(header, nestedJson.toString(),
		 * soapOutput != null ? soapOutput : soapApiResponse.toString(), startDateTime,
		 * endDateTime, status, "JSON", interfaceJsonContent, Boolean.FALSE);
		 * 
		 * logger.error("End : callService"); return soapApiResponse; }
		 * 
		 * public String removeXmlStringNamespaceAndPreamble(String xmlString) {
		 * 
		 * logger.debug("Start : removeXmlStringNamespaceAndPreamble"); xmlString =
		 * xmlString.replaceAll(
		 * "(?s)<(\\w+):Envelope[^<>]*>.*?<\\1:Body>\\s*(.*?)\\s*</\\1:Body>\\s*</\\1:Envelope>",
		 * "$2");
		 * 
		 * logger.debug("End : removeXmlStringNamespaceAndPreamble");
		 * 
		 * return xmlString.replaceAll("(<\\?[^<]*\\?>)?",
		 * "").replaceAll(" *soapenv.*?(\"|\').*?(\"|\')", "")
		 * .replaceAll("xmlns.*?(\"|\').*?(\"|\')",
		 * "").replaceAll("encodingStyle.*?(\"|\').*?(\"|\')", "")
		 * .replaceAll("xsi.*?(\"|\').*?(\"|\')",
		 * "").replaceAll("  soapenc:arrayType=.*?(\"|\').*?(\"|\')", "")
		 * .replaceAll("(<)(\\w+:)(.*?>)", "$1$3").replaceAll("(</)(\\w+:)(.*?>)",
		 * "$1$3") .replaceAll("(<)(\\w+:)(.*?>)", "$1$3");
		 * 
		 * }
		 * 
		 * public static String filterXml(String nestedXml) {
		 * 
		 * nestedXml = nestedXml.replaceAll("&amp;", "&").replaceAll("&lt;",
		 * "<").replaceAll("&gt;", ">") .replaceAll("&apos;", "\'").replaceAll("&quot;",
		 * "\"").replaceAll("&nbsp;", " ") .replaceAll("&copy;",
		 * "@").replaceAll("&reg;", "?"); return nestedXml; }
		 * 
		 * private void getUpdatedDefaulInterfaceURL(String interfaceName, JSONObject
		 * interfaceJsonContent) { try { String apiURL =
		 * interfaceJsonContent.getString("endpointurl"); String urlStr =
		 * urlUtils.readExternalServiceURL(interfaceName); apiURL =
		 * apiURL.replace("${URL}", urlStr); interfaceJsonContent.put("endpointurl",
		 * apiURL); } catch (Exception e) { logger.error("Exception Occured::" + e); } }
		 * }
		 */