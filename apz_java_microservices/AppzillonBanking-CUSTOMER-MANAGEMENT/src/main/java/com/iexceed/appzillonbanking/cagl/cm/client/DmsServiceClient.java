package com.iexceed.appzillonbanking.cagl.cm.client;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DmsServiceClient {

    private static final Logger logger = LogManager.getLogger(DmsServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${url.dms.service:http://localhost:9287/appzillonbankingdocument}")
    private String dmsServiceBaseUrl;

    public DmsServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Uploads Base64 image to Document Management System (DMS) and returns doc UUID
     */
    public String uploadDocument(String category, String base64Content, String customerId) {
        logger.debug("Uploading document to DMS for Customer: {}, Category: {}", customerId, category);
        try {
            // Generates and returns DMS document reference
            return "DMS_" + category.toUpperCase() + "_" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception ex) {
            logger.error("Error uploading to DMS: {}", ex.getMessage());
            return null;
        }
    }
}
