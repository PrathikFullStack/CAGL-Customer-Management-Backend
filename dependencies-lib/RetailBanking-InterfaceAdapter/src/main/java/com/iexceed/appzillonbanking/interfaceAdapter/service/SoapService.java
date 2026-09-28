/*
 * package com.iexceed.appzillonbanking.interfaceAdapter.service;
 * 
 * import java.io.ByteArrayOutputStream; import java.io.IOException; import
 * java.io.StringReader; import java.io.StringWriter; import java.net.URL;
 * import java.net.URLConnection; import java.net.URLStreamHandler; import
 * java.util.ArrayList; import java.util.HashMap; import java.util.Iterator;
 * import java.util.LinkedHashMap; import java.util.List; import java.util.Map;
 * 
 * import jakarta.xml.soap.MessageFactory; import jakarta.xml.soap.MimeHeaders;
 * import jakarta.xml.soap.SOAPBody; import jakarta.xml.soap.SOAPConnection;
 * import jakarta.xml.soap.SOAPConnectionFactory; import
 * jakarta.xml.soap.SOAPEnvelope; import jakarta.xml.soap.SOAPException; import
 * jakarta.xml.soap.SOAPMessage; import jakarta.xml.soap.SOAPPart;
 * 
 * 
 * import org.apache.logging.log4j.LogManager; import
 * org.apache.logging.log4j.Logger; import org.json.JSONArray; import
 * org.json.JSONObject; import org.springframework.stereotype.Component;
 * 
 * import com.iexceed.appzillonbanking.core.constants.CommonConstants; import
 * com.iexceed.appzillonbanking.core.utils.BaseDynamicValue; import
 * com.iexceed.appzillonbanking.core.utils.CommonUtils; import
 * com.iexceed.appzillonbanking.core.utils.CustomDynamicValue; import
 * com.iexceed.appzillonbanking.interfaceAdapter.utils.ParserUtils;
 * 
 * @Component public class SoapService {
 * 
 * private static final Logger logger = LogManager.getLogger(SoapService.class);
 * 
 * 
 * Calling obtainSimpleJson method to obtain a JSON containing all the nodes and
 * its elements. The values for different interface elements are also fetched in
 * this method. Input : Java object from input and the JSON array containing the
 * interface element details and the property file path Output: JSON containing
 * all the nodes and its elements of the interface
 * 
 * public JSONObject obtainSimpleSoapJson(JSONObject reqObj, JSONArray
 * parsedArray) throws Exception {
 * 
 * JSONObject finalJson = new JSONObject(); String finalJsonString = "";
 * logger.debug("obtainSimpleSoapJson reqObj = " + reqObj);
 * 
 * BaseDynamicValue gcObj = new BaseDynamicValue(); CustomDynamicValue cuObj =
 * new CustomDynamicValue(); ParserUtils parserUtils = new ParserUtils();
 * 
 * for (int i = 0, size = parsedArray.length(); i < size; i++) { JSONObject
 * outputJson = new JSONObject(); JSONObject reqJson =
 * parsedArray.getJSONObject(i);
 * 
 * if (reqJson.has(CommonConstants.DEFAULT_VALUE) &&
 * reqJson.has(CommonConstants.RELATED_NODE)) { String defaultValue =
 * reqJson.getString(CommonConstants.DEFAULT_VALUE); String entireKey = null;
 * String[] key = null; String keyName = reqJson.has(CommonConstants.ALIAS) ?
 * (reqJson.getString(CommonConstants.ALIAS) + ":" + reqJson.getString("name"))
 * : (reqJson.getString("name")); String value = "";
 * 
 * String completeString = ""; JSONObject getValueJson = null; int diff = 0;
 * 
 * entireKey = defaultValue.substring((defaultValue.indexOf('~')) + 1); String
 * nodeName = reqJson.has(CommonConstants.ALIAS) ?
 * (reqJson.getString(CommonConstants.ALIAS) + ":" +
 * reqJson.getString(CommonConstants.RELATED_NODE)) :
 * reqJson.getString(CommonConstants.RELATED_NODE);
 * 
 * if (defaultValue.toLowerCase().contains("object")) { key =
 * entireKey.split("\\."); int length = key.length;
 * 
 * for (int j = 0; j < length; j++) { diff = length - j; if (diff == 1) { if
 * (completeString.length() > 0) { getValueJson = new
 * JSONObject(completeString);
 * 
 * if (getValueJson.get(key[j]) instanceof JSONArray) { JSONArray tempArray1 =
 * new JSONArray(); String tempValue1 = getValueJson.get(key[j]).toString();
 * String[] arrValues1 = tempValue1.split(",");
 * 
 * for (String arrValue : arrValues1) { tempArray1.put(arrValue); }
 * 
 * value = tempArray1.toString(); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } else { value = getValueJson.get(key[j]).toString(); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } } else { if (reqObj.get(key[j]) instanceof JSONArray) { JSONArray
 * tempArray3 = reqObj.getJSONArray(key[j]); value = tempArray3.toString();
 * finalJson = parserUtils.formSimpleJson(finalJson, nodeName, keyName, value,
 * outputJson); } else { value = reqObj.get(key[j]).toString(); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } }
 * 
 * } else {
 * 
 * if (j == 0) { completeString = reqObj.getJSONObject(key[j]).toString(); }
 * else { JSONObject tempObj = new JSONObject(completeString); completeString =
 * tempObj.get(key[j]).toString(); } } } } else if
 * (defaultValue.toLowerCase().contains("file")) { value =
 * CommonUtils.getExternalProperties(entireKey); if (value == null) {
 * logger.debug("Required parameters are not configured in the property file");
 * throw new
 * IOException("Please configure the required parameters in the property file");
 * }
 * 
 * outputJson.put(keyName, value); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } else if (defaultValue.toLowerCase().contains("basejavacode")) { value =
 * gcObj.generateValue(defaultValue); if (value == null) {
 * logger.debug("Error in Javacode element configuration"); throw new
 * Exception("Error in Interface Configuration"); }
 * 
 * outputJson.put(keyName, value); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } else if (defaultValue.toLowerCase().contains("customjavacode")) { value =
 * cuObj.generateValue(defaultValue); if (value == null) {
 * logger.debug("Error in Custom java element configuration"); throw new
 * Exception("Error in Interface Configuration"); }
 * 
 * outputJson.put(keyName, value); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } else if (defaultValue.toLowerCase().contains("const")) { value = entireKey;
 * outputJson.put(keyName, value); finalJson =
 * parserUtils.formSimpleJson(finalJson, nodeName, keyName, value, outputJson);
 * } } else {
 * logger.debug("Please configure the default value and related node parameters"
 * ); throw new
 * Exception("Please configure the default value and related node parameters");
 * } }
 * 
 * finalJsonString = (finalJson.toString()); finalJson = new
 * JSONObject(finalJsonString); return finalJson; }
 * 
 * public SOAPMessage createSoapRequest(String soapAction, String xml) throws
 * SOAPException, ParserConfigurationException, SAXException, IOException {
 * 
 * MessageFactory messageFactory =
 * MessageFactory.newInstance((CommonUtils.getExternalProperties("soapProtocol")
 * )); SOAPMessage soapMessage = messageFactory.createMessage();
 * 
 * SOAPPart soapPart = soapMessage.getSOAPPart(); SOAPEnvelope envelope =
 * soapPart.getEnvelope(); SOAPBody soapBody = envelope.getBody();
 * 
 * MimeHeaders headers = soapMessage.getMimeHeaders();
 * headers.addHeader(CommonConstants.SOAP_ACTION, soapAction);
 * 
 * DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 * factory.setNamespaceAware(true); DocumentBuilder builder =
 * factory.newDocumentBuilder(); Document doc = builder.parse(new
 * InputSource(new StringReader(xml))); soapBody.addDocument(doc);
 * 
 * soapMessage.saveChanges();
 * 
 * ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
 * soapMessage.writeTo(requestStream);
 * 
 * return soapMessage; }
 * 
 * public String addNamespace(String ignoreNamespace, String namespace, String
 * xmlData) throws ParserConfigurationException, SAXException, IOException,
 * TransformerException {
 * 
 * String output = null;
 * 
 * DocumentBuilder builder =
 * DocumentBuilderFactory.newInstance().newDocumentBuilder(); Document document
 * = builder.parse(new InputSource(new StringReader(xmlData)));
 * 
 * Element originalDocumentElement = document.getDocumentElement();
 * 
 * Element newDocumentElement =
 * document.createElement(originalDocumentElement.getNodeName());
 * 
 * if ("N".equalsIgnoreCase(ignoreNamespace)) {
 * 
 * Map<String, String> lNameSpacesUriAttributesMap =
 * getNameSpaceUriMap(namespace);
 * 
 * for (Map.Entry<String, String> entry :
 * lNameSpacesUriAttributesMap.entrySet()) {
 * newDocumentElement.setAttribute(entry.getKey(), entry.getValue()); } }
 * 
 * NodeList list = originalDocumentElement.getChildNodes(); while
 * (list.getLength() != 0) {
 * 
 * newDocumentElement.appendChild(list.item(0));
 * 
 * }
 * 
 * document.removeChild(originalDocumentElement);
 * document.appendChild(newDocumentElement);
 * 
 * Source src = new DOMSource(document); TransformerFactory tranFactory =
 * TransformerFactory.newInstance();
 * 
 * Transformer aTransformer = tranFactory.newTransformer(); StringWriter writer
 * = new StringWriter(); Result dest = new StreamResult(writer);
 * aTransformer.transform(src, dest); writer.flush(); output =
 * writer.toString();
 * 
 * return output; }
 * 
 * private Map<String, String> getNameSpaceUriMap(String nameSpaces) { String[]
 * nameSpacesSplit = split(nameSpaces, CommonConstants.XMLNS_COLON); Map<String,
 * String> attributes = new HashMap<String, String>();
 * 
 * for (int i = 1; i < nameSpacesSplit.length; i++) { String[] nameSpace =
 * split(nameSpacesSplit[i], "="); String nameSpaceUri =
 * nameSpace[1].substring(1, nameSpace[1].lastIndexOf('\"')).trim();
 * 
 * if (nameSpaceUri.endsWith("\"")) { if (nameSpaceUri.startsWith("\"")) {
 * nameSpaceUri = nameSpaceUri.substring(1, nameSpaceUri.length()).trim(); }
 * 
 * nameSpaceUri = nameSpaceUri.substring(0,
 * nameSpaceUri.lastIndexOf('\"')).trim(); } else { if
 * (nameSpaceUri.startsWith("\"")) { nameSpaceUri = nameSpaceUri.substring(1,
 * nameSpaceUri.length()).trim(); }
 * 
 * nameSpaceUri = nameSpaceUri.trim(); }
 * 
 * attributes.put(CommonConstants.XMLNS_COLON + nameSpace[0], nameSpaceUri); }
 * 
 * return attributes; }
 * 
 * public String executeSoapApi(String soapAction, String soapRequestObject,
 * String soapEndpointUrl) throws SOAPException, IOException,
 * ParserConfigurationException, SAXException {
 * 
 * String soapStringResp = null; ByteArrayOutputStream responseStream = null;
 * 
 * SOAPConnectionFactory soapConnectionFactory =
 * SOAPConnectionFactory.newInstance(); SOAPConnection soapConnection =
 * soapConnectionFactory.createConnection();
 * 
 * URL finalEndPoint = new URL(null, soapEndpointUrl, new URLStreamHandler() {
 * 
 * @Override protected URLConnection openConnection(URL url) throws IOException
 * { URL target = new URL(url.toString()); URLConnection connection =
 * target.openConnection(); // Connection settings
 * connection.setConnectTimeout(Integer.parseInt(CommonUtils.
 * getExternalProperties("connectTimeout")));
 * connection.setReadTimeout(Integer.parseInt(CommonUtils.getExternalProperties(
 * "readTimeout"))); return (connection); } });
 * 
 * SOAPMessage soapResponse = soapConnection.call(createSoapRequest(soapAction,
 * soapRequestObject), finalEndPoint);
 * 
 * if (soapResponse != null) { responseStream = new ByteArrayOutputStream();
 * soapResponse.writeTo(responseStream); soapStringResp =
 * responseStream.toString(); }
 * 
 * soapConnection.close();
 * 
 * if (responseStream != null) { responseStream.close(); }
 * 
 * return soapStringResp; }
 * 
 * public String[] split(String original, String separator) { List<String> nodes
 * = new ArrayList<String>(); String[] result = null; String lOriginal =
 * original; try { int index = lOriginal.indexOf(separator); while (index >= 0)
 * { nodes.add(lOriginal.substring(0, index)); lOriginal =
 * lOriginal.substring(index + separator.length()); index =
 * lOriginal.indexOf(separator); } if (!"".equals(lOriginal.trim())) {
 * nodes.add(lOriginal); } result = new String[nodes.size()]; if
 * (!nodes.isEmpty()) { for (int loop = 0; loop < nodes.size(); loop++) {
 * result[loop] = nodes.get(loop); } } } catch (Exception e) {
 * logger.error(e.toString()); } return result; }
 * 
 * public LinkedHashMap<String, ArrayList<String>>
 * obtainSoapInterfaceNodes(JSONObject fileJson) { JSONObject reqJson = new
 * JSONObject(); JSONArray reqJsonArray = null;
 * 
 * LinkedHashMap<String, ArrayList<String>> hmap = new LinkedHashMap<String,
 * ArrayList<String>>(); Iterator<String> keys = fileJson.keys();
 * 
 * while (keys.hasNext()) { String key = keys.next();
 * 
 * if (fileJson.get(key) instanceof JSONArray) { reqJsonArray =
 * fileJson.getJSONArray(key);
 * 
 * for (int i = 0; i < reqJsonArray.length(); i++) { reqJson = (JSONObject)
 * reqJsonArray.get(i); String type = reqJson.get("type").toString();
 * 
 * if (type.equalsIgnoreCase("ELEMENT")) { String parentNode =
 * reqJson.has("alias") ? (reqJson.get("alias").toString() + ":" +
 * reqJson.get("relatednode").toString()) :
 * (reqJson.get("relatednode").toString()); String childNode =
 * reqJson.has("alias") ? (reqJson.get("alias").toString() + ":" +
 * reqJson.get("name").toString()) : (reqJson.get("name").toString());
 * 
 * if (hmap.containsKey(parentNode)) { ArrayList<String> childNodeList =
 * hmap.get(parentNode); childNodeList.add(childNode);
 * 
 * hmap.remove(parentNode); hmap.put(parentNode, childNodeList); } else {
 * ArrayList<String> childNodeList = new ArrayList<String>();
 * childNodeList.add(childNode); hmap.put(parentNode, childNodeList); } } } } }
 * return hmap; } }
 */