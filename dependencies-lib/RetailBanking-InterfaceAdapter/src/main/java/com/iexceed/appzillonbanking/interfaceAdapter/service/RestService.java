package com.iexceed.appzillonbanking.interfaceAdapter.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.core.constants.CommonConstants;
import com.iexceed.appzillonbanking.core.exception.CustomException;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.logs.service.LogExternalReqRes;

import reactor.core.publisher.Mono;

@Component
public class RestService {

	private static final Logger logger = LogManager.getLogger(RestService.class);

	@Autowired
	private WebClient webClient;

	@Autowired
	private LogExternalReqRes logExternalReqRes;

	Object patchResponse = "";

	public static final String HTTP_ACCEPT_TYPE = "acceptType";
	public static final String AUTH_TOKEN = "authToken";
	public static final String APPLICATION = "application";

	public Mono<Object> executeRestApi(String restRequest, JSONArray headerParams, String methodType,
			String mediaTypeString, String endPointUrl, JSONObject interfaceJsonContent, Header header,
			Boolean isJSONAdapterCall, String interfaceName, String apiRequest) {
		logger.warn("apiRequest Object" + apiRequest);
		logger.warn("Start : executeRestApi with request= " + restRequest);

		Mono<Object> webClientAPIResponse = Mono.empty();
		StringBuilder builder = new StringBuilder(endPointUrl);
		URI uri = URI.create(builder.toString());
		logger.warn("API URL::" + uri);
		LocalDateTime startDateTime = LocalDateTime.now();
		// Code change added to set API timeout based on the interface file.
		int apiTimeout = 60;
		if (interfaceJsonContent.has("timeout") && !"".equalsIgnoreCase(interfaceJsonContent.getString("timeout"))) {
			apiTimeout = Integer.valueOf(interfaceJsonContent.getString("timeout"));
		}
		logger.warn("Configured API Timeout::{}", apiTimeout);
		Timestamp requestTs = new Timestamp(System.currentTimeMillis());
		if ("POST".equalsIgnoreCase(methodType)) {
			if (MediaType.MULTIPART_FORM_DATA_VALUE.equalsIgnoreCase(mediaTypeString)) {
				MultiValueMap<String, String> multipartRequest = new LinkedMultiValueMap<>();
				JSONObject formReqObject = new JSONObject(restRequest);
				Iterator<String> keys = formReqObject.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = formReqObject.get(key).toString();
					multipartRequest.add(key, value);
				}

				webClientAPIResponse = webClient.post().uri(uri)
						.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString))
						.body(BodyInserters.fromMultipartData(multipartRequest)).retrieve()
						.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
						.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
						.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
							String responseStr = getAPIResponseStrFormat(value);
							logger.debug("ExternalResponse : {}", responseStr);
							logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
									header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
						}).doOnError(error -> {
							logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
									interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
						}).flatMap(val -> {
							String response = (String) val;
							Object respObj = response;
							return Mono.just(respObj);
						});
			} else {
				webClientAPIResponse = webClient.post().uri(uri)
						.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString))
						.body(BodyInserters.fromValue(restRequest)).retrieve()
						.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
						.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
						.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
							String responseStr = getAPIResponseStrFormat(value);
							logger.debug("ExternalResponse : {}", responseStr);
							logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
									header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
						}).doOnError(error -> {
							logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
									interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
						}).onErrorResume(WebClientRequestException.class, ex -> {
							// Handle low-level issues like premature close
							logger.error("WebClientRequestException: {}");
							return Mono.empty(); // fallback or alternative response
						}).onErrorResume(Throwable.class, ex -> {
							// Handle any other unexpected exceptions
							logger.error("Unexpected error during WebClient call: {}");
							return Mono.empty();
						});

			}
		} else if ("GET".equalsIgnoreCase(methodType)) {
			if ("fetchCbReport".equalsIgnoreCase(interfaceName)) {
				webClientAPIResponse = WebClient.builder()
						.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build()
						.get().uri(uri).retrieve().bodyToMono(byte[].class)
						.map(bytes -> Base64.getEncoder().encodeToString(bytes));
			} else {
				webClientAPIResponse = webClient.get().uri(uri)
						.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString)).retrieve()
						.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
						.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
						.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
							String responseStr = getAPIResponseStrFormat(value);
							logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
									header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
						}).doOnError(error -> {
							logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
									interfaceJsonContent, isJSONAdapterCall);
							logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
									interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
						});
			}

		} else if ("PUT".equalsIgnoreCase(methodType)) {
			webClientAPIResponse = webClient.put().uri(uri)
					.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString))
					.body(BodyInserters.fromValue(restRequest)).retrieve()
					.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
					.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
					.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
						String responseStr = getAPIResponseStrFormat(value);
						logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
								header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
					}).doOnError(error -> {
						logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
								interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
					});
		} else if ("PATCH".equalsIgnoreCase(methodType)) {
			webClientAPIResponse = webClient.patch().uri(uri)
					.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString))
					.body(BodyInserters.fromValue(restRequest)).retrieve()
					.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
					.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
					.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
						String responseStr = getAPIResponseStrFormat(value);
						logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
								header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
					}).doOnError(error -> {
						logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
								interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
					});
			return webClientAPIResponse;
		} else if ("DELETE".equalsIgnoreCase(methodType)) {
			webClientAPIResponse = webClient.delete().uri(uri)
					.headers(httpHeaders(headerParams, endPointUrl, mediaTypeString)).retrieve()
					.onStatus(HttpStatusCode::is5xxServerError, RestService::httpStatusErrResponse)
					.onStatus(HttpStatusCode::is4xxClientError, RestService::httpStatusErrResponse)
					.bodyToMono(Object.class).timeout(Duration.ofSeconds(apiTimeout)).doOnSuccess(value -> {
						String responseStr = getAPIResponseStrFormat(value);
						logTxnDtlsToDB(header, restRequest, responseStr, startDateTime, "S", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, responseStr, interfaceName,
								header, requestTs, CommonConstants.EXT_API_SUC_STATUS, apiRequest);
					}).doOnError(error -> {
						logTxnDtlsToDB(header, restRequest, error.getMessage(), startDateTime, "F", "JSON",
								interfaceJsonContent, isJSONAdapterCall);
						logExternalAPIRequestResponse(interfaceJsonContent, restRequest, error.toString(),
								interfaceName, header, requestTs, CommonConstants.EXT_API_ERR_STATUS, apiRequest);
					});
		}
		return webClientAPIResponse;

	}

	private String getAPIResponseStrFormat(Object value) {
		String response = value.toString();
		if (value instanceof HashMap<?, ?> || value instanceof ArrayList<?>) {
			try {
				response = new ObjectMapper().writeValueAsString(value);
			} catch (Exception e) {
				logger.error("Exception Occured::", e);
			}
		}
		return response;
	}

	private void logExternalAPIRequestResponse(JSONObject interfaceJsonContent, String restRequest, String response,
			String interfaceName, Header header, Timestamp requestTs, String status, String apiRequest) {
		logExternalReqRes.logExternalApiRequestResponse(interfaceJsonContent, restRequest, response, interfaceName,
				header, requestTs, status, apiRequest);
	}

	private static Mono<Error> httpStatusErrResponse(ClientResponse response) {

		logger.debug("Inside method for handling the httpStatusErrResponse" + response);
		return response.bodyToMono(String.class).flatMap(body -> {
			logger.debug("Inside 4xx or 5xx Error Response Body is {}" + body);
			JSONObject apiErrRespJSON = new JSONObject();
			int statusCode = response.statusCode().value();
			apiErrRespJSON.put("errorCode", String.valueOf(statusCode));
			apiErrRespJSON.put("errorMessage", new JSONObject(body));
			return Mono.error(new CustomException(apiErrRespJSON.toString()));
		});
	}

	private void logTxnDtlsToDB(Header header, String restRequest, String response, LocalDateTime startDateTime,
			String status, String requestType, JSONObject interfaceJsonContent, Boolean isJSONAdapterCall) {
		logExternalReqRes.logTransactionToDb(header, restRequest, response, startDateTime, LocalDateTime.now(), status,
				requestType, interfaceJsonContent, isJSONAdapterCall);
	}

	private Consumer<HttpHeaders> httpHeaders(JSONArray headersArr, String endPointUrl, String mediaTypeString) {
		return headers -> {

			// Set default Headers based on the media Type
			if (mediaTypeString.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
				headers.setContentType(new MediaType(APPLICATION, "json", StandardCharsets.UTF_8));
				headers.setAccept(Arrays.asList(new MediaType(APPLICATION, "json", StandardCharsets.UTF_8)));
			} else if (mediaTypeString.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) {
				headers.setContentType(new MediaType(APPLICATION, "xml", StandardCharsets.UTF_8));
				headers.setAccept(Arrays.asList(new MediaType(APPLICATION, "xml", StandardCharsets.UTF_8)));
			} else if (mediaTypeString.equalsIgnoreCase(MediaType.TEXT_HTML_VALUE)) {
				headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
				headers.setAccept(Arrays.asList(new MediaType("text", "html", StandardCharsets.UTF_8)));
			} else if (mediaTypeString.equalsIgnoreCase(MediaType.MULTIPART_FORM_DATA_VALUE)) {
				headers.setContentType(new MediaType("multipart", "form-data", StandardCharsets.UTF_8));
				headers.setAccept(Arrays.asList(new MediaType("multipart", "form-data", StandardCharsets.UTF_8)));
			} else {
				headers.setAccept(Arrays.asList(MediaType.ALL));
			}
			// Set Additional Headers configured as part of the Interface/JSON file.
			logger.debug("Headers Array::", headersArr);
			for (int i = 0; i < headersArr.length(); i++) {
				JSONObject tempObj = headersArr.getJSONObject(i);
				String name = tempObj.get("name").toString();
				String value = tempObj.get("value").toString();
				headers.set(name, value);
			}
			logger.debug("Final Header Parameters value::" + headers.toString());
		};
	}
}
