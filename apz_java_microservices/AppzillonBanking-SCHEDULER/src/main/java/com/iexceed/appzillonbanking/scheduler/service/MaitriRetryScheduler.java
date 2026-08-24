package com.iexceed.appzillonbanking.scheduler.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;
import com.iexceed.appzillonbanking.scheduler.domain.ab.SchedulerAuditLog;
import com.iexceed.appzillonbanking.scheduler.domain.ab.TbUacoMaitriRetry;
import com.iexceed.appzillonbanking.scheduler.model.MaitriUPIRequest;
import com.iexceed.appzillonbanking.scheduler.repository.ab.SchedulerAuditLogRepository;
import com.iexceed.appzillonbanking.scheduler.repository.ab.TbUacoMaitriRetryRepo;
import com.iexceed.appzillonbanking.scheduler.repository.ab.TbUacoQRDtlsRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaitriRetryScheduler {

    @Autowired
    private TbUacoMaitriRetryRepo tbUacoMaitriRetryRepo;

    private static final Logger logger = LogManager.getLogger(MaitriRetryScheduler.class.getName());

    @Autowired
    private InterfaceAdapter interfaceAdapter;

    @Autowired
    private SchedulerAuditLogRepository auditLogRepository;

    @Autowired
    private TbUacoQRDtlsRepo tbUacoQRDtlsRepo;

    @Value("${ab.common.maxRetryCounts}")
    private Integer maxRetryCounts;

    @Scheduled(cron = "${ab.common.maitriRetrySchedulerCron}")
    public void scheduleTask() {SchedulerAuditLog auditLog = new SchedulerAuditLog();

        LocalDateTime startTime = LocalDateTime.now();
        auditLog.setSchedulerName("MaitriRetryScheduler");
        auditLog.setStartTime(startTime);
        auditLog.setCreatedAt(startTime);

        try {
            logger.debug("Maitri Retry Scheduler started at {}", startTime);
            int count = retryTransactions(auditLog);
            auditLog.setStatus("SUCCESS");
            auditLog.setRecordCount(count);

        } catch (Exception e) {
            logger.error("Exception in MaitriRetryScheduler", e);
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(e.getMessage());

        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            auditLog.setEndTime(endTime);
            auditLogRepository.save(auditLog);
            logger.debug("Maitri Retry Scheduler ended at {}", endTime);
        }
    }

    private int retryTransactions(SchedulerAuditLog auditLog) {
        logger.debug("========= Inside Maitri Retry Scheduler =========");
        int retryLimit = (maxRetryCounts == null)
                        ? 3
                        : maxRetryCounts;

        List<TbUacoMaitriRetry> retryList = tbUacoMaitriRetryRepo.findBySchedulerStatus("PENDING");
        if (retryList.isEmpty()) {
            logger.debug("No pending Maitri retry records found");
            return 0;
        }
        List<String> processedIds = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        int successCount = 0;

        for (TbUacoMaitriRetry retry : retryList) {

            try {
                logger.debug("Processing retry for customerId :: {}", retry.getCustomerId());
                processedIds.add(retry.getCustomerId());

                if (retry.getRetryCount() >= retryLimit) {
                    retry.setSchedulerStatus("FAILED");
                    retry.setUpdateTs(new Timestamp(System.currentTimeMillis()));
                    tbUacoMaitriRetryRepo.save(retry);
                    logger.debug("Retry limit exceeded for customerId :: {}", retry.getCustomerId());
                    continue;
                }

                MaitriUPIRequest request = mapper.readValue(retry.getRequestPayload(), MaitriUPIRequest.class);
                Header header = new Header();
                Mono<Object> responseMono = interfaceAdapter.callExternalService(header, request,
                                "MaitriUpiCollection", true);
                Object responseObj = responseMono.block();
                String responseJson = mapper.writeValueAsString(responseObj);
                JsonNode rootNode = mapper.readTree(responseJson);
                String maitriStatus = rootNode.path("header").path("status").asText();

                retry.setMaitriStatus(maitriStatus);
                retry.setResponsePayload(responseJson);
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setSchedulerStatus("success".equalsIgnoreCase(maitriStatus) ? "COMPLETED" : "PENDING");
                retry.setUpdateTs(new Timestamp(System.currentTimeMillis()));

                tbUacoMaitriRetryRepo.save(retry);
                if ("success".equalsIgnoreCase(maitriStatus)) {
                    tbUacoQRDtlsRepo.findByBillNumber(retry.getBillNumber()).ifPresent(qrDtls -> {
                                qrDtls.setMaitriStatus("success");
                                tbUacoQRDtlsRepo.save(qrDtls);
                                logger.debug("Updated QR maitri status for bill number :: {}", retry.getBillNumber());
                            });
                }
                successCount++;
                logger.debug("Retry completed for customerId :: {} status :: {}", retry.getCustomerId(), maitriStatus);

            } catch (Exception ex) {

                logger.error("Retry failed for customerId :: {}", retry.getCustomerId(), ex);

                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setSchedulerStatus("PENDING");
                retry.setMaitriStatus("FAILED");
                retry.setResponsePayload(ex.getMessage());
                retry.setUpdateTs(new Timestamp(System.currentTimeMillis()));
                tbUacoMaitriRetryRepo.save(retry);
            }
        }
        auditLog.setProcessedIds(String.join(",", processedIds));
        return successCount;
    }
}
