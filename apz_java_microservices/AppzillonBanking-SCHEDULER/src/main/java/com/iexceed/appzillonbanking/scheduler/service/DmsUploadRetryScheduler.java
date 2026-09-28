package com.iexceed.appzillonbanking.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.scheduler.model.*;
import com.iexceed.appzillonbanking.scheduler.dao.DMSServiceStatusDAO;
import com.iexceed.appzillonbanking.scheduler.domain.ab.SchedulerAuditLog;
import com.iexceed.appzillonbanking.scheduler.repository.ab.SchedulerAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DmsUploadRetryScheduler {

	private static final Logger logger = LoggerFactory.getLogger(DmsUploadRetryScheduler.class.getName());

	// Limit response body logs to avoid flooding and leaking PII
	private static final int LOG_BODY_LIMIT = 1500;

	// Consider moving to CommonConstants if used elsewhere
	// private static final String DEVICE_ID = "WEB";

	public static final String FETCH_DMSSERVICE_STATUS_INTERFACEDID = "DMSServices";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private DMSServiceStatusDAO dMSServiceDAO;

	// @Autowired
	// private TbObDmsFailedUploadRepo dmsFailedUploadRepo;

	@Autowired
	private SchedulerAuditLogRepository auditLogRepo;

	// @Autowired
	// private TbObApplicationMasterRepo tbObApplicationMasterRepo;

	private int intervalThreadPoolSize = 10;

	@Autowired
	private RestTemplate template;

	@Value("${spring.dmsFailedRetry.url}")
	private String dmsFailedRetryUrl;

	@Value("${ab.common.dmsUploadRetrySchedulerCron}")
	private String dmsUploadRetrySchedulerCron;

	// Scheduler 1 — tb_ob_dms_failed_upload table retry
	@Scheduled(cron = "${ab.common.dmsUploadRetrySchedulerCron}")
	public void retryFailedDmsUploads() {
		logger.debug("Inside FAILED DMS CASES :");
		LocalDateTime startTime = LocalDateTime.now();
		LocalDateTime endTime;
		String status = "SUCCESS";
		String errorMessage = null;
		List<String> processedApplicationIds = null;
		int totalProcessed = 0;
		int success = 0;
		int failure = 0;

		try {
			List<DMSServiceResponse> respList = dMSServiceDAO.fetchDMSServiceFailedErrors();
			logger.debug("Printing respList for DMS Failed cases : {}", respList);
			if (respList == null || respList.isEmpty()) {
				return;
			}
			processedApplicationIds = respList.stream().map(DMSServiceResponse::getApplicationId).toList();

			URI url = toUri(dmsFailedRetryUrl);
			logger.debug("Fetching failed DMS URL : {}", url);
			ExecutorService executor = Executors.newFixedThreadPool(intervalThreadPoolSize);
			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failureCount = new AtomicInteger(0);

			for (DMSServiceResponse resp : respList) {
				logger.debug("Testing for inside FAILED DMS CASES :");
				executor.submit(() -> sendDMSServiceFailedRequest(resp, url, successCount, failureCount));
			}
			executor.shutdown();
			try {
				if (!executor.awaitTermination(58, TimeUnit.MINUTES)) {
					executor.shutdownNow();
					logger.warn("Interval scheduler forced shutdown after timeout.");
				}
			} catch (InterruptedException ie) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
				logger.warn("Interval scheduler interrupted during shutdown.", ie);
			}
			totalProcessed = respList.size();
			success = successCount.get();
			failure = failureCount.get();
		} catch (Exception e) {
			status = "FAILURE";
			errorMessage = e.getMessage();
			logger.error("Exception in interval scheduler: {}", e.getMessage(), e);
		} finally {
			endTime = LocalDateTime.now();
			SchedulerAuditLog audit = new SchedulerAuditLog();
			audit.setSchedulerName("DmsUploadRetryScheduler(DMS Failed Upload Retry)");
			audit.setStartTime(startTime);
			audit.setEndTime(endTime);
			audit.setRecordCount(totalProcessed);
			audit.setStatus(status);
			audit.setErrorMessage(errorMessage);
			audit.setProcessedIds(processedApplicationIds == null ? null : String.join(",", processedApplicationIds));
			audit.setCreatedAt(LocalDateTime.now());
			auditLogRepo.save(audit);
			logger.debug(
					"Disbursement status check (1-hr interval) scheduler ended at {}, Total: {}, Success: {}, Failed: {}",
					endTime, totalProcessed, success, failure);
		}
	}

	private void sendDMSServiceFailedRequest(DMSServiceResponse resp, URI url, AtomicInteger successCount,
			AtomicInteger failureCount) {
		logger.debug("Printing resp for DMS Failed cases : {}", resp);
		String applicationId = resp.getApplicationId();
		try {
			// Convert stored payload JSON into object
			DmsPayloadWrapper storedPayload = objectMapper.readValue(resp.getPayload(), DmsPayloadWrapper.class);
			logger.debug("Printing storedPayload for DMS Failed cases : {}", storedPayload);

			// Get actual DMS API request
			DmsApiRequest dmsApiRequest = storedPayload.getApiRequest().getApiRequest();
			logger.debug("Printing dmsApiRequest for DMS Failed cases : {}", dmsApiRequest);

			// Build request body expected by /dms API
			DmsDocumentRequest dmsDocumentRequest = new DmsDocumentRequest();

			dmsDocumentRequest.setApiRequest(dmsApiRequest);

			// Build headers from stored DMS header
			HttpHeaders headers = buildDMSHeaders(storedPayload.getHeader());
			logger.debug("Printing dmsDocumentRequest for DMS Failed cases : {}", dmsDocumentRequest);
			// Create request entity
			RequestEntity<DmsDocumentRequest> entity = new RequestEntity<>(dmsDocumentRequest, headers, HttpMethod.POST,
					url);
			logger.debug("Printing entity for DMS Failed cases : {}", entity);
			// Request log
			if (logger.isDebugEnabled()) {
				logger.debug("DMS Retry Request: applicationId={}, url={}, headers={}, payload={}", applicationId, url,
						headers, toJsonSafe(dmsDocumentRequest));
			}
			// Execute DMS API
			ResponseEntity<String> response = template.exchange(entity, String.class);
			logger.debug("DMS API Response: applicationId={}, response={}", applicationId, response);

			logger.debug("DMS Retry Response headers for applicationId={}: {}", applicationId, response.getHeaders());
			logger.debug("DMS Retry Response body for applicationId={}: {}", applicationId,
					safeTruncate(response.getBody()));
			successCount.incrementAndGet();
		} catch (RestClientException rce) {
			failureCount.incrementAndGet();
			logger.error("DMS Retry Request failed (HTTP): applicationId={}, error={}", applicationId, rce.getMessage(),
					rce);
		} catch (Exception e) {
			failureCount.incrementAndGet();
			logger.error("DMS Retry Request failed (unexpected): applicationId={}, error={}", applicationId,
					e.getMessage(), e);
		}
	}

	private HttpHeaders buildDMSHeaders(DmsHeader dmsHeader) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (dmsHeader != null) {
			headers.set("appId", dmsHeader.getAppId());
			headers.set("interfaceId", dmsHeader.getInterfaceId());
			headers.set("userId", dmsHeader.getUserId());
			headers.set("masterTxnRefNo", dmsHeader.getMasterTxnRefNo());
			headers.set("deviceId", dmsHeader.getDeviceId());
		}
		return headers;
	}

	private String safeTruncate(String body) {
		if (body == null) {
			return null;
		}
		return body.length() > LOG_BODY_LIMIT ? body.substring(0, LOG_BODY_LIMIT) + "...(truncated)" : body;
	}

	private String toJsonSafe(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return String.valueOf(obj);
		}
	}

	private URI toUri(String value) {
		try {
			return new URI(value);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URI for spring.disbursementStatus.url: " + value, e);
		}
	}
}
