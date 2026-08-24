package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObBMReInterview;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.enums.WFStage;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObBMReInterviewRepository;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.SequenceUtil;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;

import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BMReinterviewService {

    @Autowired
    private TbObBMReInterviewRepository bmReInterviewRepository;

    @Autowired
    private TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    private TbObCustomerRepository customerRepository;

    @Autowired
    private SequenceUtil sequenceUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${onboarding.BMReinterview.default-kt-question-count:5}")
    private int defaultKtQuestionCount;

    @Value("${onboarding.BMReinterview.max-kendra-distance:500}")
    private long maxKendraDistance;

    private static final Logger logger = LogManager.getLogger(BMReinterviewService.class);

    private static final String KM = "KM";
    private static final String BM = "BM";
    private static final String LAST_SUB_STAGE = "1.5";
    private static final String SUB_STAGE_STATUS_OPENED = "OPENED";
    private static final String SUB_STAGE_STATUS_CLOSED = "CLOSED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String BM_REINTERVIEW_ID_SEQUENCE = "seq_ob_bm_reinterview_id";

    @Transactional
    public Mono<Response> submitBMReInterview(BMReInterviewRequest request, Header header) {

        logger.info("BM ReInterview API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {

            BMReInterviewRequestFields requestObj = request.getRequestObj();
            logger.info("Processing BM ReInterview for Application Id : {}", requestObj.getApplicationId());

            BMReinterviewCustomerDetails custDetails = requestObj.getCustDetails() != null
                    ? requestObj.getCustDetails() : new BMReinterviewCustomerDetails();
            String decision = custDetails.getDecision();

            if (Boolean.TRUE.equals(requestObj.getIsDraft()) && isDecisionPresent(decision)) {
                throw new RuntimeException("isDraft cannot be true when a decision is provided.");
            }

            // -->> if misMatch happen for gps if need to notified to Audit team need to enable the below logic
//            if(custDetails.getGpsMisMatchFlag() != 'N'){
            // Audit/BTS team should get notified -->> TODO
//                logger.warn("Location details mismatched between the KM & BM");
//            }
//            if(custDetails.getDistanceFromKendraFlag() != 'N'){
            // Audit/BTS team should get notified -->> TODO
//                logger.warn("customer house location distance exceeded, more than {} meters", maxKendraDistance);
//            }

            Optional<TbObBMReInterview> reInterviewOpt =
                    bmReInterviewRepository.findByApplicationId(requestObj.getApplicationId());

            TbObBMReInterview bmReInterview;

            if (reInterviewOpt.isPresent()) {
                bmReInterview = reInterviewOpt.get();
                updateExistingReInterview(bmReInterview, requestObj, custDetails, request.getUserId(), request.getUserName());
            } else {
                bmReInterview = createNewReInterview(requestObj, custDetails, request.getUserId(), request.getUserName());
            }

            bmReInterviewRepository.save(bmReInterview);

            persistKycDetails(requestObj, custDetails, request.getUserId());

            boolean revertedToCgt = false;
            if (!Boolean.TRUE.equals(requestObj.getIsDraft()) && isDecisionPresent(decision)) {
                revertedToCgt = updateApplicationStage(decision, requestObj, request.getUserId());
            }

            if (revertedToCgt) {
                responseBody.setResponseObj("Group-Id: " + requestObj.getGroupId()
                        + " successfully revert back to CGT stage");
            } else {
                responseBody.setResponseObj(buildBMResponse(bmReInterview, requestObj));
            }
            CommonUtils.generateHeaderForSuccess(responseHeader);

        } catch (Exception ex) {
            logger.error("Exception while saving BM ReInterview.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());
        }
        return Mono.just(Response.builder().responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    private TbObBMReInterview createNewReInterview(BMReInterviewRequestFields requestObj,
                                                   BMReinterviewCustomerDetails custDetails,
                                                   String bmId, String bmName) {

        logger.info("Creating BM ReInterview.");

        BMReinterviewLocationDetails kmGpsInfo = extractKmGPSInfo(requestObj.getLocCaptureBy(), custDetails);
        BMReinterviewLocationDetails bmGpsInfo = extractBmGPSInfo(requestObj.getLocCaptureBy(), custDetails);
        LocalDateTime now = LocalDateTime.now();

        return TbObBMReInterview
                .builder().reinterviewId(sequenceUtil.nextValueAsString(BM_REINTERVIEW_ID_SEQUENCE))
                .applicationId(requestObj.getApplicationId())
                .customerId(requestObj.getCustomerId())
                .bmId(bmId)      // get from userId
                .bmName(bmName)  // get from userName
                .docVerified(isDocumentVerified(custDetails))
                .docVerifiedTs(getDocumentVerifiedTs(custDetails))
                .docVerifyPayload(toJsonOrEmpty(buildDocumentPayload(custDetails)))
                .ktQuestionsCount(custDetails.getKnowledgeTest() == null ?
                        defaultKtQuestionCount : custDetails.getKnowledgeTest().getTotalQuestions())
                .ktScore(custDetails.getKnowledgeTest() == null ?
                        null : custDetails.getKnowledgeTest().getScore())
                .ktAnswers(toJsonOrEmpty(buildKtAnswers(custDetails)))
                .ktCompletedTs(custDetails.getKnowledgeTest() == null ?
                        null : epochMillisToLocalDateTime(custDetails.getKnowledgeTest().getCompletedTs()))
                .bmGpsLatitude(toBigDecimal(bmGpsInfo == null ? null : bmGpsInfo.getLat()))
                .bmGpsLongitude(toBigDecimal(bmGpsInfo == null ? null : bmGpsInfo.getLon()))
                .kmGpsLatitude(toBigDecimal(kmGpsInfo == null ? null : kmGpsInfo.getLat()))
                .kmGpsLongitude(toBigDecimal(kmGpsInfo == null ? null : kmGpsInfo.getLon()))
                .gpsDistanceBmKm(custDetails.getGpsDistanceBmKm())
                .gpsMismatchFlag(custDetails.getGpsMisMatchFlag())
                .distFromKendraM(custDetails.getDistanceFromKendra())
                .distFromKendraFlag(custDetails.getDistanceFromKendraFlag())
                .locationCapturedTs(epochMillisToLocalDateTime(custDetails.getLocationCapturedTs()))
                .housePhotoDocId(custDetails.getHousePhoto() == null ? null : custDetails.getHousePhoto().getHousePhotoDocId())
                .housePhotoClarity(custDetails.getHousePhoto() == null ? null : custDetails.getHousePhoto().getClarityScore())
                .housePhotoClarityPass(custDetails.getHousePhoto() == null ? null : custDetails.getHousePhoto().getHousePhotoClarityPass())
                .housePhotoTs(custDetails.getHousePhoto() == null ? null : epochMillisToLocalDateTime(custDetails.getHousePhoto().getHousePhotoTs()))
                .loanEditedByBm(custDetails.getLoanEditedByBm())
                .questionnaireAnswers(toJsonOrEmpty(buildQuestionnaire(custDetails)))
//                .unnatiRedirect(custDetails.getUnnatiRedirect())
                .decision(custDetails.getDecision())
                .rejectionReasons(toJsonOrEmpty(custDetails.getRejectionReasons()))
                .decisionRemarks(custDetails.getDecisionRemarks())
                .decisionTs(isDecisionPresent(custDetails.getDecision()) ? now : null)
                .status(deriveStatus(requestObj.getSubStageStatus(), null))
                .subStage(requestObj.getSubStage())
                .subStageStatus(requestObj.getSubStageStatus())
                .reinterviewStartTs(now)
                .reinterviewEndTs(isReInterviewEnding(requestObj, custDetails.getDecision()) ? now : null)
                .createdTs(now)
                .build();
    }

    private void updateExistingReInterview(TbObBMReInterview bmReInterview, BMReInterviewRequestFields requestObj,
                                           BMReinterviewCustomerDetails custDetails, String bmId, String bmName) {

        logger.info("Updating Existing BM ReInterview.");

        BMReinterviewLocationDetails kmGpsInfo = extractKmGPSInfo(requestObj.getLocCaptureBy(), custDetails);
        BMReinterviewLocationDetails bmGpsInfo = extractBmGPSInfo(requestObj.getLocCaptureBy(), custDetails);
        LocalDateTime now = LocalDateTime.now();

        bmReInterview.setBmId(bmId); // get from userId
        bmReInterview.setBmName(bmName); // get from userName
        bmReInterview.setDocVerified(isDocumentVerified(custDetails));
        bmReInterview.setDocVerifiedTs(getDocumentVerifiedTs(custDetails));
        bmReInterview.setDocVerifyPayload(toJsonOrEmpty(buildDocumentPayload(custDetails)));

        if (custDetails.getKnowledgeTest() != null) {
            bmReInterview.setKtQuestionsCount(custDetails.getKnowledgeTest().getTotalQuestions());
            bmReInterview.setKtScore(custDetails.getKnowledgeTest().getScore());
            bmReInterview.setKtAnswers(toJsonOrEmpty(buildKtAnswers(custDetails)));
            bmReInterview.setKtCompletedTs(epochMillisToLocalDateTime(custDetails.getKnowledgeTest().getCompletedTs()));
        }

        if (bmGpsInfo != null) {
            bmReInterview.setBmGpsLatitude(toBigDecimal(bmGpsInfo.getLat()));
            bmReInterview.setBmGpsLongitude(toBigDecimal(bmGpsInfo.getLon()));
        }
        if (kmGpsInfo != null) {
            bmReInterview.setKmGpsLatitude(toBigDecimal(kmGpsInfo.getLat()));
            bmReInterview.setKmGpsLongitude(toBigDecimal(kmGpsInfo.getLon()));
        }
        bmReInterview.setGpsDistanceBmKm(custDetails.getGpsDistanceBmKm());
        bmReInterview.setGpsMismatchFlag(custDetails.getGpsMisMatchFlag());
        bmReInterview.setDistFromKendraM(custDetails.getDistanceFromKendra());
        bmReInterview.setDistFromKendraFlag(custDetails.getDistanceFromKendraFlag());
        bmReInterview.setLocationCapturedTs(epochMillisToLocalDateTime(custDetails.getLocationCapturedTs()));

        if (custDetails.getHousePhoto() != null) {
            bmReInterview.setHousePhotoDocId(custDetails.getHousePhoto().getHousePhotoDocId());
            bmReInterview.setHousePhotoClarity(custDetails.getHousePhoto().getClarityScore());
            bmReInterview.setHousePhotoClarityPass(custDetails.getHousePhoto().getHousePhotoClarityPass());
            bmReInterview.setHousePhotoTs(epochMillisToLocalDateTime(custDetails.getHousePhoto().getHousePhotoTs()));
        }

        bmReInterview.setLoanEditedByBm(custDetails.getLoanEditedByBm());
        bmReInterview.setQuestionnaireAnswers(toJsonOrEmpty(buildQuestionnaire(custDetails)));
//        bmReInterview.setUnnatiRedirect(custDetails.getUnnatiRedirect());
        bmReInterview.setDecision(custDetails.getDecision());
        bmReInterview.setRejectionReasons(toJsonOrEmpty(custDetails.getRejectionReasons()));
        bmReInterview.setDecisionRemarks(custDetails.getDecisionRemarks());

        if (isDecisionPresent(custDetails.getDecision()) && bmReInterview.getDecisionTs() == null) {
            bmReInterview.setDecisionTs(now);
        }

        bmReInterview.setStatus(deriveStatus(requestObj.getSubStageStatus(), bmReInterview.getStatus()));
        bmReInterview.setSubStage(requestObj.getSubStage());
        bmReInterview.setSubStageStatus(requestObj.getSubStageStatus());

        if (bmReInterview.getReinterviewEndTs() == null
                && isReInterviewEnding(requestObj, custDetails.getDecision())) {
            bmReInterview.setReinterviewEndTs(now);
        }

        bmReInterview.setUpdatedBy(bmId);
        bmReInterview.setUpdatedTs(LocalDateTime.now());

        logger.info("Existing BM ReInterview updated successfully.");
    }

    private void persistKycDetails(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails, String userId) {

        if (custDetails.getKycDetails() == null) {
            return;
        }

        TbObCustomer customer = customerRepository.findByCustomerId(requestObj.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer Id not found in tb_ob_customer table: " + requestObj.getCustomerId()));

        BMReinterviewLocationDetails bmGpsInfo = extractBmGPSInfo(requestObj.getLocCaptureBy(), custDetails);

        BMReinterviewKycDetails kyc = custDetails.getKycDetails();
        kyc.setGpsLat(bmGpsInfo == null ? null : bmGpsInfo.getLat());
        kyc.setGetLong(bmGpsInfo == null ? null : bmGpsInfo.getLon());

        Map<String, Object> kycDetails = customer.getKycDetails() != null
                ? new LinkedHashMap<>(customer.getKycDetails()) : new LinkedHashMap<>();
        kycDetails.putAll(objectMapper.convertValue(kyc, new TypeReference<Map<String, Object>>() {}));

        customer.setKycDetails(kycDetails);
        customer.setUpdatedBy(userId);
        customer.setUpdatedTs(LocalDateTime.now());
        customerRepository.save(customer);
        logger.info("KYC details updated in tb_ob_customer for Customer Id : {}", requestObj.getCustomerId());
    }

    /**
     * locationDetails holds only whichever GPS entries locCaptureBy claims were captured: both
     * KM and BM (2 entries, [0]=km/[1]=bm) when locCaptureBy mentions both (e.g. "KMBM"), or a
     * single entry when it mentions only one of them.
     */
    private BMReinterviewLocationDetails extractKmGPSInfo(String locCaptureBy, BMReinterviewCustomerDetails custDetails) {

        List<BMReinterviewLocationDetails> locationDetails = custDetails.getLocationDetails();
        if (locCaptureBy == null || locationDetails == null || locationDetails.isEmpty()) {
            return null;
        }

        String captureBy = locCaptureBy.toUpperCase();
        boolean hasKm = captureBy.contains(KM);
        boolean hasBm = captureBy.contains(BM);

        if (hasKm && hasBm) {
            return locationDetails.get(0);
        }
        if (hasKm) {
            return locationDetails.get(0);
        }
        return null;
    }

    private BMReinterviewLocationDetails extractBmGPSInfo(String locCaptureBy, BMReinterviewCustomerDetails custDetails) {

        List<BMReinterviewLocationDetails> locationDetails = custDetails.getLocationDetails();
        if (locCaptureBy == null || locationDetails == null || locationDetails.isEmpty()) {
            return null;
        }

        String captureBy = locCaptureBy.toUpperCase();
        boolean hasKm = captureBy.contains(KM);
        boolean hasBm = captureBy.contains(BM);

        if (hasKm && hasBm) {
            return locationDetails.size() > 1 ? locationDetails.get(1) : null;
        }
        if (hasBm) {
            return locationDetails.get(0);
        }
        return null;
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            logger.warn("Invalid GPS coordinate value : {}", value);
            return null;
        }
    }

    /**
     * subStage tracks which BM Reinterview screen is active (1.1 Document Verification ... 1.5
     * Loan Details) and subStageStatus whether it's OPENED (in progress) or CLOSED (completed).
     * The record's overall status mirrors whichever subStage was last touched, regardless of
     * decision.
     */
    private String deriveStatus(String subStageStatus, String existingStatus) {

        if (SUB_STAGE_STATUS_OPENED.equalsIgnoreCase(subStageStatus)) {
            return STATUS_IN_PROGRESS;
        }
        if (SUB_STAGE_STATUS_CLOSED.equalsIgnoreCase(subStageStatus)) {
            return STATUS_COMPLETED;
        }
        return existingStatus != null ? existingStatus : STATUS_PENDING;
    }

    private boolean isReInterviewEnding(BMReInterviewRequestFields requestObj, String decision) {

        boolean lastSubStageClosed = LAST_SUB_STAGE.equals(requestObj.getSubStage())
                && SUB_STAGE_STATUS_CLOSED.equalsIgnoreCase(requestObj.getSubStageStatus());
        return lastSubStageClosed || isDecisionPresent(decision);
    }

    private boolean isDecisionPresent(String decision) {
        return decision != null && !decision.isBlank();
    }

    private boolean updateApplicationStage(String decision, BMReInterviewRequestFields requestObj, String userId) {

        logger.info("Updating Application Master.");

        if ("MOVETOCGT".equalsIgnoreCase(decision)) {
            revertGroupToCGTStage(requestObj, userId);
            return true;
        }

        Optional<TbObApplicationMaster> applicationOpt = applicationMasterRepository
                .findByApplicationId(requestObj.getApplicationId());

        if (applicationOpt.isEmpty()) {
            throw new RuntimeException("Application not found.");
        }

        TbObApplicationMaster application = applicationOpt.get();

        switch (decision.toUpperCase()) {
            case "APPROVED":
                application.setStage("6"); // GRT
                application.setWfStage(WFStage.GRT.name()); //GRT
                application.setStatus(ApplicationStatus.GRT.name());
                break;
            case "REJECTED":
                application.setWfStage(WFStage.BMREJECT.name());
                application.setStatus(ApplicationStatus.REJECTED.name());
                break;
            default:
                logger.error("Invalid decision taken by the user : {}. choose either APPROVED/MOVETOCGT/REJECTED", decision.toUpperCase());
                throw new RuntimeException("Invalid decision taken by the user. choose either APPROVED/MOVETOCGT/REJECTED");
        }

        application.setVersion(String.valueOf(Integer.parseInt(application.getVersion()) + 1));
        application.setUpdatedBy(userId);
        application.setUpdatedTs(LocalDateTime.now());
        applicationMasterRepository.save(application);
        logger.info("Application Master Updated.");
        return false;
    }

    private void revertGroupToCGTStage(BMReInterviewRequestFields requestObj, String userId) {

        logger.info("Reverting Group Id : {} back to CGT stage.", requestObj.getGroupId());

        List<TbObApplicationMaster> applications = applicationMasterRepository
                .findByGroupIdAndStatusNot(requestObj.getGroupId(), ApplicationStatus.REJECTED.name());

        if (applications.isEmpty()) {
            logger.error("No application id found in the tb_ob_application table while search by groupId. group id : {}", requestObj.getGroupId());
            throw new RuntimeException("No application found for the given Group Id.");
        }

        LocalDateTime now = LocalDateTime.now();
        String revertRemark = "Candidate and assigned group reverted back to CGT stage from BM stage.";

        for (TbObApplicationMaster application : applications) {
            application.setStage("4");  // CGT
            application.setWfStage(WFStage.CGT.name());
            application.setVersion(String.valueOf(Integer.parseInt(application.getVersion()) + 1));
            application.setStatus(ApplicationStatus.CGT.name());
            application.setUpdatedBy(userId);
            application.setUpdatedTs(now);

            Map<String, Object> addInfo1 = fromJsonOrEmptyMap(application.getAddInfo1());
            addInfo1.put("BMReinterviewRemark", revertRemark);
            application.setAddInfo1(toJsonOrEmpty(addInfo1));
        }

        applicationMasterRepository.saveAll(applications);
        logger.info("Group Id : {} reverted to CGT stage for {} records.",
                requestObj.getGroupId(), applications.size());
    }

    private Boolean isDocumentVerified(BMReinterviewCustomerDetails custDetails) {

        if (custDetails.getDocumentVerification() == null || custDetails.getDocumentVerification().isEmpty()) {
            return Boolean.FALSE;
        }

        return custDetails.getDocumentVerification()
                .stream()
                .allMatch(document ->
                        Boolean.TRUE.equals(document.getVerified()));
    }

    private LocalDateTime getDocumentVerifiedTs(BMReinterviewCustomerDetails custDetails) {

        if (custDetails.getDocumentVerification() == null) {
            return null;
        }

        Long latestVerifiedTs = custDetails.getDocumentVerification()
                .stream()
                .map(BMReinterviewDocumentVerificationRequestFields::getVerifiedTs)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);

        return epochMillisToLocalDateTime(latestVerifiedTs);
    }

    /** Converts a client-supplied epoch-millis timestamp to LocalDateTime (server's default zone), or null if absent. */
    private LocalDateTime epochMillisToLocalDateTime(Long epochMillis) {
        return epochMillis == null ? null
                : Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private List<Map<String, Object>> buildDocumentPayload(BMReinterviewCustomerDetails custDetails) {

        if (custDetails.getDocumentVerification() == null || custDetails.getDocumentVerification().isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> documentList = new ArrayList<>();
        for (BMReinterviewDocumentVerificationRequestFields document : custDetails.getDocumentVerification()) {
            Map<String, Object> documentMap = new LinkedHashMap<>();
            documentMap.put("documentId", document.getDocumentId());
            documentMap.put("documentName", document.getDocumentName());
            documentMap.put("verified", document.getVerified());
            documentMap.put("verifiedTs", document.getVerifiedTs());
            documentList.add(documentMap);
        }
        return documentList;
    }

    private List<Map<String, Object>> buildKtAnswers(BMReinterviewCustomerDetails custDetails) {

        if (custDetails.getKnowledgeTest() == null || custDetails.getKnowledgeTest().getAnswers() == null) {
            return new ArrayList<>();
        }
        return custDetails.getKnowledgeTest().getAnswers().stream()
                .map(answer -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("questionId", answer.getQuestionId());
                    map.put("question", answer.getQuestion());
                    map.put("answer", answer.getAnswer());
                    map.put("correct", answer.getCorrect());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildQuestionnaire(BMReinterviewCustomerDetails custDetails) {

        List<Map<String, Object>> questionnaire = new ArrayList<>();
        if (custDetails.getQuestionnaire() == null || custDetails.getQuestionnaire().getAnswers() == null) {
            return questionnaire;
        }
        for (BMReinterviewQuestionnaireAnswerRequestFields answer : custDetails.getQuestionnaire().getAnswers()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("question", answer.getQuestion());
            map.put("answer", answer.getAnswer());
            questionnaire.add(map);
        }
        return questionnaire;
    }

    private String toJsonOrEmpty(Object value) {

        if (value == null || (value instanceof Collection<?> collection && collection.isEmpty())) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting value to JSON", e);
        }
    }

    /**
     * Deserializes {@code tb_ob_application_master.add_info1} (now a TEXT column) back into a
     * mutable {@code Map<String, Object>} so callers can add/overwrite a key and persist it again
     * via {@link #toJsonOrEmpty}. A null/blank value is treated as an empty, mutable map.
     */
    private Map<String, Object> fromJsonOrEmptyMap(String json) {

        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing stored JSON value.", e);
        }
    }

    private String buildBMResponse(TbObBMReInterview bmReInterview, BMReInterviewRequestFields requestObj) {
        try {
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("message", "BM ReInterview submitted successfully");
            responseMap.put("reinterviewId", bmReInterview.getReinterviewId());
            responseMap.put("applicationId", bmReInterview.getApplicationId());
            responseMap.put("customerId", bmReInterview.getCustomerId());
            responseMap.put("decision", bmReInterview.getDecision());
            responseMap.put("status", bmReInterview.getStatus());
            responseMap.put("subStage", bmReInterview.getSubStage());
            responseMap.put("subStageStatus", bmReInterview.getSubStageStatus());
            responseMap.put("isDraft", Boolean.TRUE.equals(requestObj.getIsDraft()));
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing BM response.", ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }
}
