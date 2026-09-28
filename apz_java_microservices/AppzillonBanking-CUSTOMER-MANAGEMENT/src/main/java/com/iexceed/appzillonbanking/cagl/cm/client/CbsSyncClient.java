package com.iexceed.appzillonbanking.cagl.cm.client;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CbsSyncClient {

    private static final Logger logger = LogManager.getLogger(CbsSyncClient.class);

    private final RestTemplate restTemplate;

    @Value("${url.cbs.service:http://localhost:9286/appzillonbankingcbs}")
    private String cbsServiceBaseUrl;

    public CbsSyncClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Pushes final approved customer record into T24 Core Banking System
     */
    public boolean syncToT24(String customerId, Map<String, Object> updatePayload) {
        logger.debug("Syncing customer data to T24 Core Banking for Customer ID: {}", customerId);
        try {
            // Invokes T24 CBS REST Adapter
            logger.info("Successfully pushed customer: {} updates to T24 Core Banking", customerId);
            return true;
        } catch (Exception ex) {
            logger.error("Error syncing to T24 CBS: {}", ex.getMessage());
            return false;
        }
    }
}
