package com.iexceed.appzillonbanking.cagl.cob.service;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.BlacklistKendraRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.BlacklistKendraRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BlacklistKendraService {

    private static final Logger logger = LogManager.getLogger(BlacklistKendraService.class);

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kendra.blacklist.action.url}")
    private String kendraBlacklistActionUrl;

    @Transactional
    public Response updateBlacklistStatus(BlacklistKendraRequest request, Header header) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        String action = request.getRequestObj().getAction();
        String status;
        if ("BLOCK".equalsIgnoreCase(action)) {
            status = "BLOCK";
        } else if ("UNBLOCK".equalsIgnoreCase(action)) {
            status = "CLEAR";
        } else {
            CommonUtils.generateHeaderForFailure(responseHeader, "Invalid action — expected BLOCK or UNBLOCK, got: " + action);
            responseBody.setResponseObj("Invalid action.");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
            return response;
        }
        try {
            BlacklistKendraRequestFields requestObj = request.getRequestObj();

            Optional<TbObKendra> kendraOpt = kendraRepository.findById(requestObj.getKendraId());
            if (kendraOpt.isEmpty()) {
                logger.warn("No Kendra found for kendraId:{}", requestObj.getKendraId());
                CommonUtils.generateHeaderForFailure(responseHeader, "No Kendra found for the given kendraId");
                responseBody.setResponseObj("Kendra not found.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }
            TbObKendra kendra = kendraOpt.get();
            kendra.setBlacklistStatus(status);
            kendra.setBlacklistTs(LocalDateTime.now());
            kendra.setUpdatedBy(request.getUserId());
            kendra.setUpdatedTs(LocalDateTime.now());
            kendraRepository.save(kendra);

            if (requestObj.getBranchId() == null) {
                requestObj.setBranchId(kendra.getBranchId());
            }
            if (requestObj.getKendraName() == null) {
                requestObj.setKendraName(kendra.getKendraName());
            }
            if (requestObj.getLatitude() == null) {
                requestObj.setLatitude(kendra.getGpsLatitude());
            }
            if (requestObj.getLongitude() == null) {
                requestObj.setLongitude(kendra.getGpsLongitude());
            }
            if (requestObj.getVillage() == null) {
                requestObj.setVillage(kendra.getVillage());
            }
            if (requestObj.getPincode() == null) {
                requestObj.setPincode(kendra.getPincode());
            }

            logger.debug("Kendra {} blacklist_status set to {} (forwarding same value to CAGL)",
                    requestObj.getKendraId(), status);
            forwardToCagl(request, header);
            responseBody.setResponseObj("Kendra " + requestObj.getKendraId() + " " + action + " successfully.");
            CommonUtils.generateHeaderForSuccess(responseHeader);

        } catch (Exception ex) {
            logger.error("Exception while updating Kendra blacklist status", ex);
            CommonUtils.generateHeaderForFailure(responseHeader, "Unable to update Kendra blacklist status");
            responseBody.setResponseObj("Blacklist update failed.");
        }
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
        return response;
    }

    private void forwardToCagl(BlacklistKendraRequest request, Header header) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appId", header.getAppId());
        headers.set("interfaceId", header.getInterfaceId());
        headers.set("userId", header.getUserId());
        headers.set("masterTxnRefNo", header.getMasterTxnRefNo());
        headers.set("deviceId", header.getDeviceId());

        HttpEntity<BlacklistKendraRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(kendraBlacklistActionUrl, entity, Void.class);
            logger.info("Blacklist status forwarded successfully to CAGL.");
        } catch (Exception ex) {
            logger.error("Failed to forward blacklist status to CAGL.", ex);
            throw new RuntimeException("Failed to forward blacklist status to CAGL.", ex);
        }
    }

}
