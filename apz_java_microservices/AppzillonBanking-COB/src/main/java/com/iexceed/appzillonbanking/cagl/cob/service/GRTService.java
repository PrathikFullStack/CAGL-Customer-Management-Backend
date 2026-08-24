package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.enums.WFStage;
import com.iexceed.appzillonbanking.cagl.cob.exception.GRTValidationException;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGRT;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGRTRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonInliningUtil;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GRTService {

    @Autowired
    private TbObGRTRepository grtRepository;

    @Autowired
    private TbObGroupRepository groupRepository;

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    private TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    private SequenceUtil sequenceUtil;

    @Autowired
    private JsonInliningUtil jsonInliningUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${onboarding.GRTService.house-visit-minimum-percentage:50}")
    private static int houseVisitMinPct;

    @Value("${onboarding.GRTService.minimum-grt-members:5}")
    private static int minimumGRTMembers;

    private static final Logger logger = LogManager.getLogger(GRTService.class);
    public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";
    private static final BigDecimal HOUSE_VISIT_MIN_PCT = BigDecimal.valueOf(houseVisitMinPct);
    private static final List<String> VALID_DECISIONS = Arrays.asList("APPROVED", "REJECTED", "RECOMMEND_CGT");
    private static final Integer MIN_GRT_MEMBERS = minimumGRTMembers;
    private static final String ABSENT_FOR_GRT = "Absent for GRT";
    private static final String LAST_SUB_STAGE = "1.7";
    private static final String SUB_STAGE_STATUS_CLOSED = "CLOSED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final Character DEFAULT_FLAG_N = 'N';
    private static final String GRT_ID_SEQUENCE = "seq_ob_grt_schedule_id";

    @Transactional
    public Mono<Response> submitBMReInterview(GRTSubmitRequest request, Header header) {

        logger.info("GRT Submit API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {

            GRTSubmitRequestFields requestObj = request.getRequestObj();
            logger.info("Processing GRT for Group Id : {}", requestObj.getGroupId());

            boolean isDraft = Boolean.TRUE.equals(requestObj.getIsDraft());

            if (isDraft && isDecisionPresent(requestObj.getDecision())) {
                throw new GRTValidationException("isDraft cannot be true when a decision is provided.");
            }

            if (!isDraft) {
                validateRequest(requestObj, request.getUserId());
                validateGlAndKlAreNotSamePerson(requestObj.getGlCustomerId(), requestObj.getKlCustomerId());
                validateGLBelongsToGroup(requestObj.getGlCustomerId(), requestObj.getGroupId());
                validateKLBelongsToKendra(requestObj.getKlCustomerId(), requestObj.getKendraId());
                checkForGlKlExistence(requestObj.getGroupId(), requestObj.getKendraId(),
                        requestObj.getGlCustomerId(), requestObj.getKlCustomerId(), request.getUserId());
            }

            Optional<TbObGRT> grtOpt = grtRepository.findByGroupId(requestObj.getGroupId());

            TbObGRT grt;

            if (grtOpt.isPresent()) {
                logger.info("Existing GRT record found for Group Id : {}", requestObj.getGroupId());
                grt = grtOpt.get();
                updateExistingGRT(grt, requestObj, request.getUserId(), request.getUserName());
            } else {
                logger.info("Creating new GRT record.");
                grt = createNewGRT(requestObj, request.getUserId(), request.getUserName());
            }
            grtRepository.save(grt);

            if (!isDraft) {
                rejectAbsentMembers(requestObj, request.getUserId());
                updateApplicationStage(requestObj, request.getUserId());
            }

            // if AUDIT needed -->> TODO

            responseBody.setResponseObj(buildResponse(grt));
            CommonUtils.generateHeaderForSuccess(responseHeader);
        } catch (GRTValidationException ex) {
            logger.error("Validation failed.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Exception while processing GRT.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_MSG);
        }
        return Mono.just(Response.builder().responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    private boolean isDecisionPresent(String decision) {
        return decision != null && !decision.isBlank();
    }

    /**
     * subStage 1.1 .. 1.7 tracks which GRT screen is active, subStageStatus whether it's
     * OPENED or CLOSED. Only when the last subStage (1.7 - Kendra Details) is CLOSED does the
     * frontend-supplied status (COMPLETED/REJECTED) get trusted; every other update just marks
     * the record IN_PROGRESS regardless of what status the client sent.
     */
    private String deriveStatus(GRTSubmitRequestFields requestObj, String existingStatus) {

        boolean lastSubStageClosed = LAST_SUB_STAGE.equals(requestObj.getSubStage())
                && SUB_STAGE_STATUS_CLOSED.equalsIgnoreCase(requestObj.getSubStageStatus());

        if (lastSubStageClosed) {
            return requestObj.getStatus() != null ? requestObj.getStatus() : STATUS_COMPLETED;
        }
        if (requestObj.getSubStageStatus() != null) {
            return STATUS_IN_PROGRESS;
        }
        return existingStatus != null ? existingStatus : STATUS_PENDING;
    }

    private void validateRequest(GRTSubmitRequestFields requestObj, String userId) {

        logger.info("Validating GRT request.");

        validateMandatoryFields(requestObj, userId);
        validateMinimumMembers(requestObj);
        validateHouseVisit(requestObj);
        validateHouseVisitCompliance(requestObj); //  --->> based on houseVisitVerificationFlag calculate the percentage of house visit
        validateAnnexure(requestObj);
        validateDecision(requestObj);
        validateGLAndKL(requestObj);
        validateRejectionReason(requestObj);

        logger.info("GRT validation completed successfully.");
    }

    private void validateMandatoryFields(GRTSubmitRequestFields requestObj, String userId) {

        if (requestObj == null) {
            throw new GRTValidationException("Request object cannot be null.");
        }
        if (requestObj.getGroupId() == null) {
            throw new GRTValidationException("Group Id is mandatory.");
        }
        if (requestObj.getKendraId() == null) {
            throw new GRTValidationException("Kendra Id is mandatory.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new GRTValidationException("User Id is mandatory.");
        }
        if (requestObj.getDecision() == null || requestObj.getDecision().trim().isEmpty()) {
            throw new GRTValidationException("Decision is mandatory.");
        }
    }

    private void validateMinimumMembers(GRTSubmitRequestFields requestObj) {

        Integer totalMembers = getTotalMembers(requestObj);
        if (totalMembers < MIN_GRT_MEMBERS) {throw new GRTValidationException(
                "Minimum " + MIN_GRT_MEMBERS + " members are required to conduct GRT.");
        }
    }

    private void validateHouseVisit(GRTSubmitRequestFields requestObj) {

        if (requestObj.getHouseVisitPct() == null) {
            throw new GRTValidationException("House visit percentage is mandatory.");
        }
        if (requestObj.getHouseVisitPct().compareTo(HOUSE_VISIT_MIN_PCT) < 0) {
            throw new GRTValidationException("Minimum 50 percent house visit is required.");
        }
        if (requestObj.getHouseVisitPct().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new GRTValidationException("House visit percentage cannot exceed 100.");
        }
        // gpsFlagMismatch need to send the notification to BST/AUDIT team TODO
    }

    // AM-verified house visits (houseVisitVerificationFlag = true) must cover at least houseVisitMinPct
    // of the group's total members, independent of whatever houseVisitPct the client reports.
    private void validateHouseVisitCompliance(GRTSubmitRequestFields requestObj) {

        int totalMembers = getTotalMembers(requestObj);
        Integer verifiedHouseVisitCount = getVerifiedHouseVisitCount(requestObj);
        Integer minimumRequiredHouseVisits = (int) Math.ceil(totalMembers * houseVisitMinPct / 100.0);

        BigDecimal actualHouseVisitPct = totalMembers == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(verifiedHouseVisitCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalMembers), 2, RoundingMode.HALF_UP);

        logger.info("Group Id : {} - AM actually covered {}% house visits ({} out of {} members).",
                requestObj.getGroupId(), actualHouseVisitPct, verifiedHouseVisitCount, totalMembers);

        if (verifiedHouseVisitCount < minimumRequiredHouseVisits) {
            throw new GRTValidationException("Minimum " + houseVisitMinPct + " percent house visit is required. "
                    + verifiedHouseVisitCount + " out of " + totalMembers + " houses visited, "
                    + minimumRequiredHouseVisits + " required.");
        }
    }

    private Integer getVerifiedHouseVisitCount(GRTSubmitRequestFields requestObj) {

        if (requestObj.getHouseVisit() == null) {
            return 0;
        }
        return (int) requestObj.getHouseVisit()
                .stream()
                .filter(houseVisit -> Boolean.TRUE.equals(houseVisit.getHouseVisitVerificationFlag()))
                .count();
    }

    private void validateAnnexure(GRTSubmitRequestFields requestObj) {
        if (requestObj.getAnnexureDocId() == null || requestObj.getAnnexureDocId().trim().isEmpty()) {
            throw new GRTValidationException("GRT annexure document is mandatory.");
        }
    }

    private void validateDecision(GRTSubmitRequestFields requestObj) {
        if (!VALID_DECISIONS.contains(requestObj.getDecision().toUpperCase())) {
            throw new GRTValidationException("Invalid decision. Allowed values are APPROVED, REJECTED and RECOMMEND_CGT.");
        }
    }

    private void validateGLAndKL(GRTSubmitRequestFields requestObj) {
        if (requestObj.getGlCustomerId() == null) {
            throw new GRTValidationException("Group leader is mandatory.");
        }
        if (requestObj.getKlCustomerId() == null) {
            throw new GRTValidationException("Kendra leader is mandatory.");
        }
        if (requestObj.getGlCustomerId().equals(requestObj.getKlCustomerId())) {
            throw new GRTValidationException("Group leader and Kendra leader cannot be same.");
        }
    }

    private void validateRejectionReason(GRTSubmitRequestFields requestObj) {
        if ("REJECTED".equalsIgnoreCase(requestObj.getDecision())) {
            if (requestObj.getRejectionReasons() == null || requestObj.getRejectionReasons().isEmpty()) {
                throw new GRTValidationException("Rejection reason is mandatory.");
            }
        }
    }

    private TbObGRT createNewGRT(GRTSubmitRequestFields requestObj, String userId, String userName) {

        logger.info("Creating new GRT record for Group Id : {}", requestObj.getGroupId());

        return TbObGRT.builder()
                .grtId(nextCgtId())
                .groupId(requestObj.getGroupId())
                .kendraId(requestObj.getKendraId())
                .userId(userId)
                .userRole(requestObj.getUserRole())
                .amName(userName)
                .attendance(toJsonOrEmpty(requestObj.getAttendance()))
                .totalMembers(getTotalMembers(requestObj))
                .presentCount(getPresentCount(requestObj))
                .absentCount(getAbsentCount(requestObj))
                .groupPhotoDocId(requestObj.getGroupPhotoDocId())
                .groupPhotoClarity(requestObj.getGroupPhotoClarity())
                .docVerifiedMembers(toJsonOrEmpty(requestObj.getDocumentVerification()))
                .docVerificationCount(getDocumentVerificationCount(requestObj))
                .houseVisit(toJsonOrEmpty(requestObj.getHouseVisit()))
                .houseVisitCount(requestObj.getHouseVisitCount())
                .houseVisitPct(requestObj.getHouseVisitPct())
                .lnReview(toJsonOrEmpty(requestObj.getLoanReview()))
//                .loanEditedByAm(requestObj.getLoanEditedByAm())
//                .breTriggered(requestObj.getBreTriggered())
//                .breStatus(requestObj.getBreStatus())
//                .breEligibleAmt(requestObj.getBreEligibleAmt())
//                .unnatiEligible(requestObj.getUnnatiEligible())
//                .cbValidityCheck(requestObj.getCbValidityCheck())
                .questionnaireAnswers(toJsonOrEmpty(requestObj.getQuestionnaire()))
                .annexureDocId(requestObj.getAnnexureDocId())
                .annexureClarity(requestObj.getAnnexureClarity())
                .kendraMeetingDay(requestObj.getKendraMeetingDay())
                .kendraMeetingTime(requestObj.getKendraMeetingTime())
                .kendraMeetingPlace(requestObj.getKendraMeetingPlace())
                .glCustomerId(requestObj.getGlCustomerId())
                .glMobile(requestObj.getGlMobile())
                .klCustomerId(requestObj.getKlCustomerId())
                .klMobile(requestObj.getKlMobile())
                .decision(requestObj.getDecision())
                .rejectionReasons(toJsonOrEmpty(requestObj.getRejectionReasons()))
                .decisionRemarks(requestObj.getDecisionRemarks())
                .status(requestObj.getStatus())
                .subStage(requestObj.getSubStage())
                .subStageStatus(requestObj.getSubStageStatus())
                .grtStartTs(LocalDateTime.now())
                .grtEndTs(LocalDateTime.now())
                .createdTs(LocalDateTime.now())
                .build();
    }

    /** Draws the next {@code cgt_id} straight from the Postgres sequence, bypassing the JPA/Hibernate id generator. */
    private String nextCgtId() {
        return sequenceUtil.nextValueAsString(GRT_ID_SEQUENCE);
    }

    private void updateExistingGRT(TbObGRT grt, GRTSubmitRequestFields requestObj, String userId, String userName) {

        logger.info("Updating GRT record : {}", grt.getGrtId());

        grt.setUserId(userId);
        grt.setUserRole(requestObj.getUserRole());
        grt.setAmName(userName);
        grt.setAttendance(toJsonOrEmpty(requestObj.getAttendance()));
        grt.setTotalMembers(getTotalMembers(requestObj));
        grt.setPresentCount(getPresentCount(requestObj));
        grt.setAbsentCount(getAbsentCount(requestObj));
        grt.setGroupPhotoDocId(requestObj.getGroupPhotoDocId());
        grt.setGroupPhotoClarity(requestObj.getGroupPhotoClarity());
        grt.setDocVerifiedMembers(toJsonOrEmpty(requestObj.getDocumentVerification()));
        grt.setDocVerificationCount(getDocumentVerificationCount(requestObj));
        grt.setHouseVisit(toJsonOrEmpty(requestObj.getHouseVisit()));
        grt.setHouseVisitCount(requestObj.getHouseVisitCount());
        grt.setHouseVisitPct(requestObj.getHouseVisitPct());
        grt.setLnReview(toJsonOrEmpty(requestObj.getLoanReview()));
//        grt.setLoanEditedByAm(requestObj.getLoanEditedByAm());
//        grt.setBreTriggered(requestObj.getBreTriggered());
//        grt.setBreStatus(requestObj.getBreStatus());
//        grt.setBreEligibleAmt(requestObj.getBreEligibleAmt());
//        grt.setUnnatiEligible(requestObj.getUnnatiEligible());
//        grt.setCbValidityCheck(requestObj.getCbValidityCheck());
        grt.setQuestionnaireAnswers(toJsonOrEmpty(requestObj.getQuestionnaire()));
        grt.setAnnexureDocId(requestObj.getAnnexureDocId());
        grt.setAnnexureClarity(requestObj.getAnnexureClarity());
        grt.setKendraMeetingDay(requestObj.getKendraMeetingDay());
        grt.setKendraMeetingTime(requestObj.getKendraMeetingTime());
        grt.setKendraMeetingPlace(requestObj.getKendraMeetingPlace());
        grt.setGlCustomerId(requestObj.getGlCustomerId());
        grt.setGlMobile(requestObj.getGlMobile());
        grt.setKlCustomerId(requestObj.getKlCustomerId());
        grt.setKlMobile(requestObj.getKlMobile());
        grt.setDecision(requestObj.getDecision());
        grt.setRejectionReasons(toJsonOrEmpty(requestObj.getRejectionReasons()));
        grt.setDecisionRemarks(requestObj.getDecisionRemarks());
        grt.setStatus(requestObj.getStatus());
        grt.setSubStage(requestObj.getSubStage());
        grt.setSubStageStatus(requestObj.getSubStageStatus());
        grt.setGrtStartTs(LocalDateTime.now());
        grt.setGrtEndTs(LocalDateTime.now());
        grt.setUpdatedBy(userId);
        grt.setUpdatedTs(LocalDateTime.now());
        logger.info("GRT updated successfully.");
    }

    //GL should be from same group
    private void validateGLBelongsToGroup(String glCustomerId, String groupId){
        TbObApplicationMaster applicationMaster = applicationMasterRepository.findByCustomerId(glCustomerId)
                .orElseThrow(() -> new GRTValidationException("Group Id not fond while checking for the gl-ID"));
        if (!groupId.equals(applicationMaster.getGroupId())){
            throw new GRTValidationException("GL should belong to same group.");
        }
    }

    //KL should be from any of the groups under the particular kendra
    private void validateKLBelongsToKendra(String klCustomerId, String kendraId){
        TbObApplicationMaster applicationMaster = applicationMasterRepository.findByCustomerId(klCustomerId)
                .orElseThrow(() -> new GRTValidationException("Kendra Id not fond while checking for the kl-ID"));
        if (!kendraId.equals(applicationMaster.getKendraId())){
            throw new GRTValidationException("KL should belong to same kendra.");
        }
    }

    // GL and KL should not be the same person
    private void validateGlAndKlAreNotSamePerson(String glCustomerId, String klCustomerId){
        if(Objects.equals(glCustomerId, klCustomerId)){
            throw new GRTValidationException("GL and KL should not be the same person.");
        }
    }

    //check for existence of KL and GL - GL lives on tb_ob_group.group_leader_id, KL lives on tb_ob_kendra.kendra_leader_id.
    //application_master is intentionally left untouched here for now.
    private void checkForGlKlExistence(String groupId, String kendraId, String glCustomerId, String klCustomerId, String userId){

        TbObGroup group = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new GRTValidationException("Group Id not fond while validating GL"));

        if (glCustomerId.equals(group.getGroupLeaderId())) {
            logger.info("Group Id : {} - GL customerId : {} already assigned, no update required.", groupId, glCustomerId);
        } else {
            logger.warn("Group Id : {} - GL changing from customerId : {} to customerId : {}. Updating tb_ob_group.",
                    groupId, group.getGroupLeaderId(), glCustomerId);
            group.setGroupLeaderId(glCustomerId);
            group.setUpdatedBy(userId);
            group.setUpdatedTs(LocalDateTime.now()); // wants need to change the LocalDateTime
            groupRepository.save(group);
            logger.info("Group Id : {} - GL updated successfully to customerId : {}.", groupId, glCustomerId);
        }

        TbObKendra kendra = kendraRepository.findByKendraId(kendraId)
                .orElseThrow(() -> new GRTValidationException("Kendra Id not fond while validating KL"));

        if (klCustomerId.equals(kendra.getKendraLeaderId())) {
            logger.info("Kendra Id : {} - KL customerId : {} already assigned, no update required.", kendraId, klCustomerId);
        } else {
            logger.warn("Kendra Id : {} - KL changing from customerId : {} to customerId : {}. Updating tb_ob_kendra.",
                    kendraId, kendra.getKendraLeaderId(), klCustomerId);
            kendra.setKendraLeaderId(klCustomerId);
            kendra.setUpdatedBy(userId);
            kendra.setUpdatedTs(LocalDateTime.now());
            kendraRepository.save(kendra);
            logger.info("Kendra Id : {} - KL updated successfully to customerId : {}.", kendraId, klCustomerId);
        }
    }

    private Integer getTotalMembers(GRTSubmitRequestFields requestObj) {
        if (requestObj.getAttendance() == null) {
            return 0;
        }
        return requestObj.getAttendance().size();

    }

    private Integer getPresentCount(GRTSubmitRequestFields requestObj) {

        if (requestObj.getAttendance() == null) {
            return 0;
        }
        return requestObj.getAttendance().size() - getAbsentCount(requestObj);
    }
    private Integer getAbsentCount(GRTSubmitRequestFields requestObj) {

        if (requestObj.getAttendance() == null) {
            return 0;
        }
        return (int) requestObj.getAttendance()
                .stream()
                .filter(attendance -> Boolean.FALSE.equals(attendance.getPresent()))
                .count();
    }

    private Integer getDocumentVerificationCount(GRTSubmitRequestFields requestObj) {
        if (requestObj.getDocumentVerification() == null) {
            return 0;
        }
        return (int) requestObj.getDocumentVerification()
                .stream()
                .filter(docVerification -> Boolean.TRUE.equals(docVerification.getVerified()))
                .count();
    }

    /**
     * Serializes any GRT sub-payload (attendance/houseVisit/documentVerification/questionnaire/
     * rejectionReasons/loanReview) straight to its JSON string form for storage in the now-TEXT
     * tb_ob_grt columns. The request DTOs already carry the exact @JsonProperty names the column
     * previously stored via hand-built maps, so no intermediate Map/List conversion is needed.
     * A null value or empty collection is stored as "" rather than the literal "null"/"[]".
     */
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

    private void updateApplicationStage(GRTSubmitRequestFields requestObj, String userId) {

        logger.info("Updating application stage for Group Id : {}", requestObj.getGroupId());

        if ("RECOMMEND_CGT".equalsIgnoreCase(requestObj.getDecision())) {
            recommendGroupToCGT(requestObj, userId);
            return;
        }

        List<TbObApplicationMaster> applications = applicationMasterRepository
                .findByGroupIdAndStatusNot(requestObj.getGroupId(), ApplicationStatus.REJECTED.name());

        if (applications.isEmpty()) {
            throw new GRTValidationException("No applications found for Group Id : " + requestObj.getGroupId());
        }
        
        // Validation for minimum members
        if(applications.size() < MIN_GRT_MEMBERS & requestObj.getDecision().equalsIgnoreCase("APPROVED")){
            throw new GRTValidationException("Minimum " + MIN_GRT_MEMBERS + " people are required in the group to complete GRT flow, groupId: " + requestObj.getGroupId());
        }

        LocalDateTime now = LocalDateTime.now();

        for (TbObApplicationMaster application : applications) {

            switch (requestObj.getDecision().toUpperCase()) {
                case "APPROVED":
                    application.setStage("7"); // ACTIVATED
                    application.setWfStage(WFStage.GRTAPPROVED.name());
                    application.setStatus(ApplicationStatus.GRTAPPROVED.name());
                    break;
                case "REJECTED":
                    application.setStatus(ApplicationStatus.REJECTED.name());
                    break;
                default:
                    throw new GRTValidationException("Invalid decision : " + requestObj.getDecision());
            }
            application.setVersion(String.valueOf(Integer.parseInt(application.getVersion()) + 1));
            application.setUpdatedBy(userId);
            application.setUpdatedTs(now);
        }
        applicationMasterRepository.saveAll(applications);
        logger.info("Application stage updated successfully.");
    }

    private void recommendGroupToCGT(GRTSubmitRequestFields requestObj, String userId) {

        logger.info("Reverting Group Id : {} to CGT stage.", requestObj.getGroupId());

        List<TbObApplicationMaster> applications = applicationMasterRepository
                .findByGroupIdAndStatusNot(requestObj.getGroupId(), ApplicationStatus.REJECTED.name());

        if (applications.isEmpty()) {
            throw new GRTValidationException("No applications found for Group Id : " + requestObj.getGroupId());
        }

        LocalDateTime now = LocalDateTime.now();

        for (TbObApplicationMaster application : applications) {
            application.setStage("4"); // CGT
            application.setWfStage(WFStage.CGT.name());
            application.setVersion(String.valueOf(Integer.parseInt(application.getVersion()) + 1));
            application.setStatus(ApplicationStatus.CGT.name());
            application.setUpdatedBy(userId);
            application.setUpdatedTs(now);
            Map<String, Object> addInfo = fromJsonOrEmptyMap(application.getAddInfo1());
            addInfo.put("GRT", "Group reverted to CGT by AM.");
            application.setAddInfo1(toJsonOrEmpty(addInfo));
        }
        applicationMasterRepository.saveAll(applications);
        logger.info("Group {} reverted to CGT.", requestObj.getGroupId());
    }

    private void rejectAbsentMembers(GRTSubmitRequestFields requestObj, String userId) {

        if (requestObj.getAttendance() == null || requestObj.getAttendance().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (GRTAttendanceRequestFields attendance : requestObj.getAttendance())
        {
            if (Boolean.FALSE.equals(attendance.getPresent())) {
                TbObApplicationMaster application = applicationMasterRepository
                        .findByCustomerId(attendance.getCustomerId())
                        .orElseThrow(() -> new GRTValidationException(
                                "Application not found for customer : "
                                        + attendance.getCustomerId()));

                application.setStatus(ApplicationStatus.REJECTED.name());
                application.setRemarks(ABSENT_FOR_GRT);
                application.setUpdatedBy(userId);
                application.setUpdatedTs(now);
                applicationMasterRepository.save(application);
                logger.info("Customer {} rejected due to absence in GRT.", attendance.getCustomerId());
            }
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

    private String buildResponse(TbObGRT grt) {

        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "GRT submitted successfully");
            response.put("grtId", grt.getGrtId());
            response.put("groupId", grt.getGroupId());
            response.put("kendraId", grt.getKendraId());
            response.put("decision", grt.getDecision());
            response.put("status", grt.getStatus());
            return objectMapper.writeValueAsString(response);

        } catch (Exception ex) {
            logger.error("Error while preparing response.", ex);
            throw new GRTValidationException("Unable to prepare response.");
        }
    }


    /**
     * Read-only lookup of a group's recorded GRT progress. Returns a "no result" response
     * (not an error) when nothing has been saved yet for the group -- that simply means GRT
     * hasn't started, which is a normal state, not a failure.
     */
    public Mono<Response> fetchGRTDetails(String groupId, Header header) {

        logger.info("Fetch GRT Details API Started.");

        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            if (groupId == null) {
                throw new GRTValidationException("Group Id is mandatory.");
            }

            logger.info("Fetching GRT Details for Group Id : {}", groupId);

            Optional<TbObGRT> grtOpt = grtRepository.findByGroupId(groupId);

            if (grtOpt.isPresent()) {
                responseBody.setResponseObj(buildFetchResponse(grtOpt.get()));
                CommonUtils.generateHeaderForSuccess(responseHeader);
            } else {
                logger.info("No GRT Details found for Group Id : {}", groupId);
                CommonUtils.generateHeaderForNoResult(responseHeader);
            }

        } catch (GRTValidationException ex) {
            logger.error("GRT Fetch Validation Failed.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());

        } catch (Exception ex) {
            logger.error("Exception while fetching GRT Details.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_MSG);
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /**
     * Builds the JSON response for a fetch, after flagging a mismatch (see
     * {@link #flagAttendanceMismatch}) between the recorded attendance count and the group's
     * current (non-rejected) member count.
     */
    private String buildFetchResponse(TbObGRT grt) {
        try {
            List<TbObApplicationMaster> groupMembers = applicationMasterRepository
                    .findByGroupIdAndStatusNot(grt.getGroupId(), ApplicationStatus.REJECTED.name());

            String attendanceMismatch = flagAttendanceMismatch(grt.getAttendance(), groupMembers.size());

            Map<String, Object> responseMap = objectMapper.convertValue(grt, new TypeReference<Map<String, Object>>() {});
            jsonInliningUtil.inlineStoredJsonStrings(responseMap);
            if (attendanceMismatch != null) {
                responseMap.put("attendanceMismatch", attendanceMismatch);
            }
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing fetch response.", ex);
            throw new GRTValidationException("Unable to prepare response.");
        }
    }

    /**
     * tb_ob_grt.attendance only holds whichever members the AM actually captured attendance for.
     * If a member joined the group later (or was skipped), that count won't match the group's
     * current (non-rejected) member count. Rather than fabricating an entry for whoever's missing,
     * a mismatch remark is returned so the caller knows the counts disagree -- this only affects
     * the response, nothing is written back to tb_ob_grt.
     */

    private String flagAttendanceMismatch(String attendance, int groupMemberCount) {

        int attendanceCount = 0;

        if (attendance != null && !attendance.isBlank()) {
            try {
                // Only the element count is needed, so read it as a tree instead of
                // deserializing every attendance entry into a Map.
                attendanceCount = objectMapper.readTree(attendance).size();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse attendance JSON", e);
            }
        }

        if (attendanceCount != groupMemberCount) {
            return "misMatch: attendance member count (" + attendanceCount
                    + ") does not match group member count (" + groupMemberCount + ").";
        }
        return null;
    }
}
