package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLead;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLeadRepository;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    @Autowired
    private TbObLeadRepository leadRepository;

    @Autowired
    private TbObApplicationMasterRepository tbObApplicationMasterRepository;

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${lead.create.url}")
    private String LeadCreateUrl;

    @Value("${lead.reject.url}")
    private String leadRejectUrl;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(LeadService.class);


    @Transactional
    public Response createLeads(CreateLeadRequest request, Header header) {

        logger.info("Inside createLeads");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        try {
            CreateLeadRequestFields requestObj = request.getRequestObj();
            validateSelection(requestObj);
            validateDuplicateRequest(requestObj);
            Response duplicateResponse = validateDatabaseDuplicate(requestObj);
            if (duplicateResponse != null) {
                return duplicateResponse;
            }
            List<TbObLead> savedLeads = saveLeadRecords(requestObj, request.getUserId());
            // If this throws an exception, saveLeadRecords() will be rolled back
            invokeCaglLeadCreation(request, header, savedLeads);
            responseBody.setResponseObj(objectMapper.writeValueAsString(buildSuccessResponse(savedLeads)));
            CommonUtils.generateHeaderForSuccess(responseHeader);
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);

        } catch (Exception ex) {
            logger.error("Exception while creating Lead", ex);
            responseBody.setResponseObj("Unable to create Lead.");
            CommonUtils.generateHeaderForFailure(responseHeader, "Exception occurred while creating Lead");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        return response;
    }
    private void validateSelection(CreateLeadRequestFields requestObj) {
        logger.info("Inside validateSelection");
        long selectedCount = requestObj.getRecords()
                .stream()
                .filter(CreateLeadRequestFields.LeadRecord::isSelectedForOnboarding)
                .count();
        if (selectedCount > 1) {
            throw new IllegalArgumentException(
                    "Only one record can be selected for onboarding.");
        }
    }
    private void validateDuplicateRequest(CreateLeadRequestFields requestObj) {
        logger.info("Inside validateDuplicateRequest");
        Set<String> uniqueRecords = new HashSet<>();
        for (CreateLeadRequestFields.LeadRecord record : requestObj.getRecords()) {
            String key = record.getMemberMobile().trim() + "_"
                    + record.getMemberName().trim().toLowerCase();
            if (!uniqueRecords.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate lead records found in request.");
            }
        }
    }
    private Response validateDatabaseDuplicate(CreateLeadRequestFields requestObj) throws JsonProcessingException {
        logger.info("Inside validateDatabaseDuplicate");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        for (CreateLeadRequestFields.LeadRecord record : requestObj.getRecords()) {
            Optional<TbObLead> duplicateLead =
                    leadRepository.findDuplicate(record.getMemberMobile(), "OPEN");
            if (duplicateLead.isPresent()) {
                responseBody.setResponseObj(
                        objectMapper.writeValueAsString(buildDuplicateResponse(duplicateLead.get())));
                CommonUtils.generateHeaderForFailure(responseHeader, "Lead already exists.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }
            Optional<TbObApplicationMaster> customer =
                    tbObApplicationMasterRepository.findByMobileNumber(record.getMemberMobile());
            if (customer.isPresent()) {
                CommonUtils.generateHeaderForFailure(responseHeader, "Customer already exists.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }
        }
        return null;
    }
    private List<TbObLead> saveLeadRecords(CreateLeadRequestFields requestObj, String userId) {
        logger.info("Inside saveLeadRecords");
        LocalDateTime currentTime = LocalDateTime.now();
        List<TbObLead> leadList = new ArrayList<>();
        for (CreateLeadRequestFields.LeadRecord record : requestObj.getRecords()) {
            TbObLead lead = buildLeadEntity(
                    record, requestObj.getBranchId(), userId, currentTime);
            leadList.add(lead);
        }
            List<TbObLead> savedLeads = leadRepository.saveAll(leadList);
        savedLeads.forEach(this::auditLeadCreation);
        logger.info("{} Lead(s) saved successfully", savedLeads.size());
        return savedLeads;
    }
    private TbObLead buildLeadEntity(CreateLeadRequestFields.LeadRecord record, String branchId, String userId, LocalDateTime currentTime) {

        String leadId = generateLeadId(userId);
        record.setLeadId(leadId);
        TbObLead lead = new TbObLead();
        lead.setLeadId(leadId);
        lead.setLeadSource(StringUtils.defaultIfBlank(record.getLeadSource(), "MAITRI"));
        lead.setCustomerName(record.getMemberName());
        lead.setMobileNumber(record.getMemberMobile());
        lead.setBranchId(branchId);
        lead.setKmId(userId);
        lead.setStatus("OPEN");
        lead.setCreatedBy(userId);
        lead.setCreatedTs(currentTime);
        lead.setUpdatedBy(userId);
        lead.setUpdatedTs(currentTime);
        lead.setAddInfo(buildAddInfo(record));
        return lead;
    }
    private Map<String, Object> buildAddInfo(CreateLeadRequestFields.LeadRecord record) {

        Map<String, Object> addInfo = new LinkedHashMap<>();
        addIfPresent(addInfo, "villageAreaName", record.getVillageAreaName());
        addIfPresent(addInfo, "landmark", record.getLandmark());
        addIfPresent(addInfo, "referredBy", record.getReferredBy());
        addIfPresent(addInfo, "referenceMobile", record.getReferenceMobile());
        addIfPresent(addInfo, "kycType", record.getKycType());
        addIfPresent(addInfo, "kycId", record.getKycId());
        return addInfo;
    }
    private void addIfPresent(Map<String, Object> addInfo, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            addInfo.put(key, value.trim());
        }
    }
    private String generateLeadId(String userId) {
        return "LE-" + userId + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
    }
    private List<LeadCreationResult> buildSuccessResponse(List<TbObLead> savedLeads) {
        logger.info("Inside buildSuccessResponse");
        List<LeadCreationResult> responseList = new ArrayList<>();
        for (TbObLead lead : savedLeads) {
            responseList.add(LeadCreationResult.builder()
                            .leadId(lead.getLeadId())
                            .memberName(lead.getCustomerName())
                            .mobileNumber(lead.getMobileNumber())
                            .message("Lead captured successfully.")
                            .build());
        }
        return responseList;
    }
    private List<LeadCreationResult> buildDuplicateResponse(TbObLead lead) {
        List<LeadCreationResult> responseList = new ArrayList<>();
        responseList.add(LeadCreationResult.builder()
                        .leadId(lead.getLeadId())
                        .memberName(lead.getCustomerName())
                        .mobileNumber(lead.getMobileNumber())
                        .message("Lead already exists. Click 'Recapture Details' to continue.")
                        .build());
        return responseList;
    }
    private void invokeCaglLeadCreation(CreateLeadRequest request, Header header, List<TbObLead> savedLeads) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appId", header.getAppId());
        headers.set("interfaceId", header.getInterfaceId());
        headers.set("userId", header.getUserId());
        headers.set("masterTxnRefNo", header.getMasterTxnRefNo());
        headers.set("deviceId", header.getDeviceId());

        HttpEntity<CreateLeadRequest> entity =
                new HttpEntity<>(request, headers);
        try {
            ResponseEntity<ResponseWrapper> cdhResponse =
                    restTemplate.postForEntity(LeadCreateUrl, entity, ResponseWrapper.class);
            logger.info("Lead creation request sent successfully to CAGL.");
            applyCdhReferences(savedLeads, cdhResponse.getBody());
        } catch (Exception ex) {
            logger.error("Failed to create lead in CAGL.", ex);
            throw new RuntimeException("Failed to create lead in CAGL.", ex);
        }
    }

    private void applyCdhReferences(List<TbObLead> savedLeads, ResponseWrapper cdhResponseWrapper) {
        try {
            if (cdhResponseWrapper == null || cdhResponseWrapper.getApiResponse() == null
                    || cdhResponseWrapper.getApiResponse().getResponseBody() == null) {
                logger.warn("CAGL response missing responseBody — u_id not updated for this batch.");
                return;
            }
            String responseObj = cdhResponseWrapper.getApiResponse().getResponseBody().getResponseObj();
            List<LeadCreationResult> references = objectMapper.readValue(responseObj,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LeadCreationResult.class));

            Map<String, TbObLead> byLeadId = savedLeads.stream()
                    .collect(Collectors.toMap(TbObLead::getLeadId, lead -> lead));

            List<TbObLead> toUpdate = new ArrayList<>();
            for (LeadCreationResult ref : references) {
                TbObLead lead = byLeadId.get(ref.getLeadId());
                if (lead != null && ref.getUId() != null) {
                    lead.setUId(ref.getUId());
                    toUpdate.add(lead);
                } else {
                    logger.warn("No matching saved lead found for CAGL reference leadId:{}", ref.getLeadId());
                }
            }            if (!toUpdate.isEmpty()) {
                leadRepository.saveAll(toUpdate);
                logger.info("Updated u_id for {} lead(s) from CAGL response.", toUpdate.size());
            }
        } catch (Exception ex) {
            // Deliberately NOT rethrown — a failure to parse/apply CAGL's
            // uId reference shouldn't roll back an otherwise-successful
            // lead creation. Confirm this is the behavior you want; if
            // u_id being set is critical, this should probably fail loudly
            // instead of just logging.
            logger.error("Failed to apply CAGL uId references onto saved leads.", ex);
        }
    }

    private void auditLeadCreation(TbObLead lead) {
        logger.info("Lead Created Successfully");
        logger.info("LeadId : {}", lead.getLeadId());
        logger.info("Customer : {}", lead.getCustomerName());
        logger.info("Mobile : {}", lead.getMobileNumber());
        // TODO
        // Save into Audit Table
    }

    @Transactional
    public Response rejectLead(RejectLeadRequest request, Header header) {
        logger.info("Inside rejectLead");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            RejectLeadRequestFields requestObj = request.getRequestObj();

            Optional<TbObLead> leadOpt = leadRepository.findByLeadId(requestObj.getLeadId());
            if (leadOpt.isEmpty()) {
                logger.warn("No lead found for leadId:{}", requestObj.getLeadId());
                CommonUtils.generateHeaderForFailure(responseHeader, "No lead found for the given leadId");
                responseBody.setResponseObj("Lead not found.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }

            TbObLead lead = leadOpt.get();
            lead.setStatus("REJECTED");
            lead.setRemarks(requestObj.getRejectReason());
            lead.setUpdatedBy(request.getUserId());
            lead.setUpdatedTs(LocalDateTime.now());
            leadRepository.save(lead);

            logger.debug("Lead rejected in COB :: leadId:{}, rejectedBy:{}", requestObj.getLeadId(), request.getUserId());
            invokeCaglLeadReject(request, header);

            responseBody.setResponseObj("Lead rejected successfully.");
            CommonUtils.generateHeaderForSuccess(responseHeader);

        } catch (Exception ex) {
            logger.error("Exception while rejecting lead", ex);
            CommonUtils.generateHeaderForFailure(responseHeader, "Unable to reject lead");
            responseBody.setResponseObj("Lead rejection failed.");
        }
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
        return response;
    }

    private void invokeCaglLeadReject(RejectLeadRequest request, Header header) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appId", header.getAppId());
        headers.set("interfaceId", header.getInterfaceId());
        headers.set("userId", header.getUserId());
        headers.set("masterTxnRefNo", header.getMasterTxnRefNo());
        headers.set("deviceId", header.getDeviceId());

        HttpEntity<RejectLeadRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(leadRejectUrl, entity, Void.class);
            logger.info("Lead reject request sent successfully to CAGL.");
        } catch (Exception ex) {
            logger.error("Failed to reject lead in CAGL.", ex);
            throw new RuntimeException("Failed to reject lead in CAGL.", ex);
        }
    }
}
