package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.enums.WFStage;
import com.iexceed.appzillonbanking.cagl.cob.exception.CGTDateValidationException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CGTService {

    @Autowired
    private TbObCGTDetailsRepository cgtRepository;

    @Autowired
    private TbObCustomerRepository customerRepository;

    @Autowired
    private TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    private TbObGroupRepository groupRepository;

    @Autowired
    private TbObLoanRepository loanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SequenceUtil sequenceUtil;

    @Value("${onboarding.CGTService.cgt-mandatory-days:3}")
    private int cgtMandatoryDays;

    @Value("${onboarding.CGTService.cgt-minimum-members:5}")
    private int cgtMinimumMembers;

    private static final String CGT_ID_SEQUENCE = "seq_ob_cgt_schedule_id";
    private static final String STAGE_CGT = "CGT";

    public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";
    public static final String EXCEPTION_OCCURED = "Exception occurred";

    private static final Logger logger = LogManager.getLogger(CGTService.class);

    /**
     * Handles both the actual CGT submit and the KM's draft "Save" action.
     * Draft saves skip validation and only touch tb_ob_cgt_details; a real submit runs full validation and updates related tables.
     */

    @Transactional
    public Mono<Response> conductCGT(CGTDetailsRequest request, Header header) {

        logger.info("CGT Schedule API Started.");

        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            CGTDetailsRequestFields requestObj = request.getRequestObj();

            if (requestObj == null || requestObj.getGroupId() == null) {
                throw new CGTDateValidationException("Group Id is mandatory.");
            }

            logger.info("Processing CGT for Group Id : {}", requestObj.getGroupId());

            boolean isDraft = Boolean.TRUE.equals(requestObj.getIsDraft());

            if (isDraft && Boolean.TRUE.equals(requestObj.getEndCGTFlag())) {
                throw new CGTDateValidationException("isDraft and endCGTFlag cannot both be true.");
            }

            List<CGTDayDetailsRequestFields> allDays = mergeDays(requestObj);
            String stageMovementRemark = null;

            Optional<TbObCGTDetails> tbObCGTDetailsOpt = cgtRepository.findByGroupId(requestObj.getGroupId());
            TbObCGTDetails cgtDetails;

            if (isDraft) {
                if (tbObCGTDetailsOpt.isPresent()) {
                    cgtDetails = tbObCGTDetailsOpt.get();
                    updateExistingCGT(cgtDetails, requestObj, allDays, request.getUserId(), cgtDetails.getStatus(), requestObj.getSubStage());
                } else {
                    cgtDetails = createNewCGT(requestObj, allDays, request.getUserId(), requestObj.getStatus(), requestObj.getSubStage());
                }
                cgtRepository.save(cgtDetails);
            } else {
                validateScheduleDates(allDays);
//                validateMinimumMembers(allDays);
                validateLearningSession(allDays);

                // status=COMPLETED means the KM is marking CGT as done, which triggers the strict mandatory-day checks and stage movement.
                boolean isCompleting = "COMPLETED".equalsIgnoreCase(requestObj.getStatus());

                if (isCompleting || requestObj.getStatus().equalsIgnoreCase("C"+cgtMandatoryDays)) {
                    validateWithMandatoryDays(allDays);
                    validateAnnexureAndLoanCapture(allDays);
                }

                if (tbObCGTDetailsOpt.isPresent()) {
                    cgtDetails = tbObCGTDetailsOpt.get();
                    updateExistingCGT(cgtDetails, requestObj, allDays, request.getUserId(), requestObj.getStatus(), requestObj.getSubStage());
                } else {
                    cgtDetails = createNewCGT(requestObj, allDays, request.getUserId(), requestObj.getStatus(), requestObj.getSubStage());
                }
                cgtRepository.save(cgtDetails);

                // Drives the attendance/learning-session merge for every submitted member below.
                List<String> customerIds = extractCustomerIds(allDays);

                String cgtStatus = computeCgtStatus(allDays, isCompleting);
                logger.info("CGT Status : {}",cgtStatus);

                updateCustomerCGTDetails(requestObj, allDays, customerIds, request.getUserId(), cgtDetails.getCgtId(), cgtStatus);

                if (isCompleting) {
                    List<String> mandatoryDayApplicationIds = extractMandatoryDayApplicationIds(allDays);
                    List<TbObApplicationMaster> loanCapturedApplications = mandatoryDayApplicationIds.isEmpty()
                            ? Collections.emptyList()
                            : applicationMasterRepository.findByApplicationIdIn(mandatoryDayApplicationIds);
                    stageMovementRemark = updateApplicationMasterStage(loanCapturedApplications, request.getUserId());
                }

                updateGroupCgtDetails(requestObj, cgtStatus, request.getUserId());
            }

            // Audit -->> TODO

            responseBody.setResponseObj(buildScheduleResponse(cgtDetails, requestObj, stageMovementRemark));
            CommonUtils.generateHeaderForSuccess(responseHeader);

        } catch (CGTDateValidationException ex) {
            logger.error("CGT Validation Failed.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());

        } catch (Exception ex) {
            logger.error("Exception while scheduling CGT.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_OCCURED);
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /** Merges conductCGTPayload and addCGTPayload into a single list of days for this request. */
    private List<CGTDayDetailsRequestFields> mergeDays(CGTDetailsRequestFields requestObj) {
        List<CGTDayDetailsRequestFields> allDays = new ArrayList<>();
        if (requestObj.getConductCGTPayload() != null) {
            allDays.addAll(requestObj.getConductCGTPayload());
        }
        if (requestObj.getAddCGTPayload() != null) {
            allDays.addAll(requestObj.getAddCGTPayload());
        }
        return allDays;
    }

    /** Returns distinct customer IDs found anywhere across all days in this submission. */
    private List<String> extractCustomerIds(List<CGTDayDetailsRequestFields> allDays) {
        return allDays.stream()
                .filter(day -> day.getMemberAttendanceDetails() != null)
                .flatMap(day -> day.getMemberAttendanceDetails().stream())
                .map(CGTMemberAttendanceDetailsRequestFields::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns distinct application IDs from the mandatory day's memberLoanDetails.
     * These are the only members eligible for stage movement to BM Re-Interview.
     */
    private List<String> extractMandatoryDayApplicationIds(List<CGTDayDetailsRequestFields> allDays) {
        return allDays.stream()
                .filter(day -> day.getDay() != null && day.getDay() == cgtMandatoryDays)
                .filter(day -> day.getMemberLoanDetails() != null)
                .flatMap(day -> day.getMemberLoanDetails().stream())
                .map(CGTMemberLoanDetailsFields::getApplicationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Checks that every submitted day has at least the minimum required members. */
    private void validateMinimumMembers(List<CGTDayDetailsRequestFields> allDays) {

        logger.info("Validating Minimum Members per CGT Day.");

        for (CGTDayDetailsRequestFields day : allDays) {
            List<CGTMemberAttendanceDetailsRequestFields> members = day.getMemberAttendanceDetails();
            if (members == null || members.size() < cgtMinimumMembers) {
                throw new CGTDateValidationException(
                        "Minimum " + cgtMinimumMembers + " members are mandatory for Day-" + day.getDay() + ".");
            }
        }

        logger.info("Minimum Members Validation Completed.");
    }

    /** Checks that every submitted day has learning session details recorded. */
    private void validateLearningSession(List<CGTDayDetailsRequestFields> allDays) {

        logger.info("Validating Learning Session per CGT Day.");

        for (CGTDayDetailsRequestFields day : allDays) {
            if (day.getLearningSession() == null || day.getLearningSession().isEmpty()) {
                throw new CGTDateValidationException(
                        "Learning session details are mandatory for Day-" + day.getDay() + ".");
            }
        }

        logger.info("Learning Session Validation Completed.");
    }

    /** Checks that all mandatory CGT days are present when status is COMPLETED. */
    private void validateWithMandatoryDays(List<CGTDayDetailsRequestFields> allDays) {

        logger.info("Validating Mandatory CGT Days.");

        Set<Integer> daySet = allDays.stream()
                .map(CGTDayDetailsRequestFields::getDay)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (daySet.size() < cgtMandatoryDays) {
            throw new CGTDateValidationException(
                    "Minimum " + cgtMandatoryDays + " CGT days are mandatory.");
        }

        for (int i = 1; i <= cgtMandatoryDays; i++) {
            if (!daySet.contains(i)) {
                throw new CGTDateValidationException(
                        "Day-" + i + " is mandatory.");
            }
        }

        logger.info("Mandatory CGT Days Validation Completed.");
    }

    /** Passes a copy of allDays to validateCGTSchedule so the original list order isn't disturbed. */
    private void validateScheduleDates(List<CGTDayDetailsRequestFields> allDays) {
        logger.info("Validating CGT Dates.");

        if (!allDays.isEmpty()) {
            validateCGTSchedule(new ArrayList<>(allDays));
        }

        logger.info("CGT Date Validation Completed.");
    }

    /** Checks day ordering: Day-1 must exist, no days skipped, dates increase, and last day isn't in the future. */
    private void validateCGTSchedule(List<CGTDayDetailsRequestFields> dayDetails) {

        logger.info("Validating CGT Schedule.");
        dayDetails.sort(Comparator.comparing(CGTDayDetailsRequestFields::getDay));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (dayDetails.get(0).getDay() != 1) {
            throw new CGTDateValidationException("Day-1 details are mandatory.");
        }

        LocalDate previousDate = null;
        LocalDate lastDate = null;
        Integer previousDayNumber = null;

        for (CGTDayDetailsRequestFields day : dayDetails) {

            if (previousDayNumber != null && day.getDay() != previousDayNumber + 1) {
                throw new CGTDateValidationException(
                        "Day-" + (previousDayNumber + 1) + " details are missing.");
            }

            LocalDate currentDate = LocalDate.parse(day.getDate(), formatter);
            if (previousDate != null && currentDate.isBefore(previousDate.plusDays(1))) {
                throw new CGTDateValidationException(
                        "Day-" + day.getDay()
                                + " should be at least one day after Day-"
                                + (day.getDay() - 1));
            }
            previousDate = currentDate;
            lastDate = currentDate;
            previousDayNumber = day.getDay();
        }

        if (lastDate != null && lastDate.isAfter(LocalDate.now())) {
            throw new CGTDateValidationException(
                    "Last CGT day should not be greater than current date.");
        }
        logger.info("CGT Schedule validation completed.");
    }

    /**
     * Checks the final mandatory day has an annexure, loan capture flag, and enough member loan details.
     * Also cross-checks the loan capture claim against tb_ob_loan.
     */
    private void validateAnnexureAndLoanCapture(List<CGTDayDetailsRequestFields> allDays) {

        logger.info("Validating Annexure and Loan Details Capture.");

        for (CGTDayDetailsRequestFields day : allDays) {
            if (day.getDay() == cgtMandatoryDays) {
                if (day.getAnnexureId() == null || day.getAnnexureId().trim().isEmpty()) {
                    throw new CGTDateValidationException(
                            "Annexure is mandatory for Day-" + cgtMandatoryDays + ".");
                }
                if (!Boolean.TRUE.equals(day.getLoanDetailsCapture())) {
                    throw new CGTDateValidationException(
                            "Loan details capture is mandatory for Day-" + cgtMandatoryDays + ".");
                }
                if (day.getMemberLoanDetails() == null || day.getMemberLoanDetails().isEmpty()) {
                    throw new CGTDateValidationException(
                            "Member loan details are mandatory for Day-" + cgtMandatoryDays + ".");
                }
                if (day.getMemberLoanDetails().size() < cgtMinimumMembers) {
                    throw new CGTDateValidationException(
                            "Minimum " + cgtMinimumMembers + " members with loan details are mandatory for Day-"
                                    + cgtMandatoryDays + ".");
                }
                validateLoanDetailsCaptured(day);
            }
//            if extra days needs annexure validation need to enable the below logic
//            else if (day.getDay() > cgtMandatoryDays)
//            {
//                if (day.getAnnexureId() == null || day.getAnnexureId().trim().isEmpty()) {
//                    throw new CGTDateValidationException(
//                            "Annexure is mandatory for Day-" + day.getDay() + ".");
//                }
//            }
        }

        logger.info("Annexure and Loan Details Capture Validation Completed.");
    }

    /** Verifies every member in memberLoanDetails actually has a loan row in tb_ob_loan. */
    private void validateLoanDetailsCaptured(CGTDayDetailsRequestFields day) {

        if (day.getMemberLoanDetails() == null || day.getMemberLoanDetails().isEmpty()) {
            return;
        }

        List<String> customerIds = day.getMemberLoanDetails().stream()
                .map(CGTMemberLoanDetailsFields::getCustomerId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());

        if (customerIds.isEmpty()) {
            return;
        }

        Set<String> customerIdsWithLoan = loanRepository.findByCustomerIdIn(customerIds).stream()
                .map(TbObLoan::getCustomerId)
                .collect(Collectors.toSet());

        List<String> missingLoanDetails = customerIds.stream()
                .filter(customerId -> !customerIdsWithLoan.contains(customerId))
                .collect(Collectors.toList());

        if (!missingLoanDetails.isEmpty()) {
            logger.warn("Loan details not found in tb_ob_loan for Customer Id(s) : {}", missingLoanDetails);
            throw new CGTDateValidationException(
                    "Loan details not found in tb_ob_loan for Customer Id(s) : " + missingLoanDetails
                            + " on Day-" + cgtMandatoryDays + ".");
        }
    }

    /** First-ever submission for this group: builds a fresh tb_ob_cgt_details row. */
    private TbObCGTDetails createNewCGT(CGTDetailsRequestFields mergedObj, List<CGTDayDetailsRequestFields> allDays, String userId, String status, List<Map<String, Object>> subStage) {

        logger.info("Creating New CGT Details.");

        TbObCGTDetails cgtDetails = TbObCGTDetails.builder()
                .cgtId(nextCgtId())
                .groupId(mergedObj.getGroupId())
                .kendraId(mergedObj.getKendraId())
                .addCGTPayload(mergedObj.getAddCGTPayload() != null ? new ArrayList<>(mergedObj.getAddCGTPayload()) : null)
                .conductCGTPayload(mergedObj.getConductCGTPayload() != null ? new ArrayList<>(mergedObj.getConductCGTPayload()) : null)
                .status(status != null ? status : "PENDING")
                .subStage(serializeSubStage(subStage))
                .endCgtTs("COMPLETED".equalsIgnoreCase(status) ? LocalDateTime.now() : null)
                .annexureId(buildAnnexureId(null, allDays))
                .groupPhotoId(buildGroupPhotoId(null, allDays))
                .createdBy(userId)
                .updatedBy(null)
                .updatedTs(null)
                .build();

        logger.info("New CGT Details Created.");
        return cgtDetails;
    }

    /** Draws the next cgt_id straight from the Postgres sequence, bypassing the JPA/Hibernate id generator. */
    private String nextCgtId() {
        return sequenceUtil.nextValueAsString(CGT_ID_SEQUENCE);
    }

    /** Updates an existing CGT record; day payloads are replaced, annexure/group-photo are merged. */
    private void updateExistingCGT(TbObCGTDetails cgtDetails, CGTDetailsRequestFields mergedObj, List<CGTDayDetailsRequestFields> allDays, String userId, String status, List<Map<String, Object>> subStage) {

        logger.info("Updating Existing CGT Details.");

        cgtDetails.setAddCGTPayload(mergedObj.getAddCGTPayload() != null ? new ArrayList<>(mergedObj.getAddCGTPayload()) : null);
        cgtDetails.setConductCGTPayload(mergedObj.getConductCGTPayload() != null ? new ArrayList<>(mergedObj.getConductCGTPayload()) : null);
        cgtDetails.setStatus(status != null ? status : cgtDetails.getStatus());
        cgtDetails.setSubStage(serializeSubStage(subStage));
        if ("COMPLETED".equalsIgnoreCase(status)) {
            cgtDetails.setEndCgtTs(LocalDateTime.now());
        }
        cgtDetails.setAnnexureId(buildAnnexureId(cgtDetails.getAnnexureId(), allDays));
        cgtDetails.setGroupPhotoId(buildGroupPhotoId(cgtDetails.getGroupPhotoId(), allDays));
        cgtDetails.setUpdatedBy(userId);
        cgtDetails.setUpdatedTs(LocalDateTime.now());
        logger.info("CGT Details Updated Successfully.");
    }

    /**
     * Updates each member's own tb_ob_customer row with this submission's attendance, learning topics, and CGT status.
     * Re-submitting the same day updates it in place instead of duplicating it.
     */
    private void updateCustomerCGTDetails(CGTDetailsRequestFields requestObj, List<CGTDayDetailsRequestFields> allDays,
                                          List<String> customerIds, String userId, String cgtId, String cgtStatus) {

        logger.info("Updating Customer CGT Details.");

        if (allDays.isEmpty()) {
            logger.info("No CGT Day Details found.");
            return;
        }

        if (customerIds.isEmpty()) {
            logger.info("No members found in CGT Day Details.");
            return;
        }

        Map<String, TbObCustomer> existingMap = customerRepository
                .findByCustomerIdIn(customerIds)
                .stream()
                .collect(Collectors.toMap(TbObCustomer::getCustomerId, c -> c));

        LocalDateTime now = LocalDateTime.now();
        Map<String, TbObCustomer> toSaveMap = new LinkedHashMap<>();

        for (CGTDayDetailsRequestFields day : allDays) {

            if (day.getMemberAttendanceDetails() == null || day.getMemberAttendanceDetails().isEmpty()) {
                continue;
            }

            for (CGTMemberAttendanceDetailsRequestFields member : day.getMemberAttendanceDetails()) {

                if (member.getCustomerId() == null) {
                    logger.warn("Skipping member with null Customer Id for Day-{}.", day.getDay());
                    continue;
                }

                TbObCustomer customer = existingMap.get(member.getCustomerId());

                if (customer == null) {
                    logger.warn("Customer Id : {} not found in tb_ob_customer; skipping CGT detail update for Day-{}.",
                            member.getCustomerId(), day.getDay());
                    continue;
                }

                try {
                    List<Map<String, Object>> attendanceList = fromJsonOrEmptyList(customer.getAttendance());

                    boolean attendanceUpdated = false;
                    for (Map<String, Object> record : attendanceList) {
                        if (Objects.equals(record.get("day"), day.getDay())) {
                            record.put("stage", STAGE_CGT);
                            record.put("date", day.getDate());
                            record.put("present", member.getPresent());
                            record.put("markedBy", userId);
                            attendanceUpdated = true;
                            break;
                        }
                    }
                    if (!attendanceUpdated) {
                        Map<String, Object> attendance = new LinkedHashMap<>();
                        attendance.put("day", day.getDay());
                        attendance.put("stage", STAGE_CGT);
                        attendance.put("date", day.getDate());
                        attendance.put("present", member.getPresent());
                        attendance.put("markedBy", userId);
                        attendanceList.add(attendance);
                    }
                    customer.setAttendance(toJsonOrEmpty(attendanceList));

                    List<Map<String, Object>> learningList = fromJsonOrEmptyList(customer.getLearningSession());

                    boolean learningUpdated = false;
                    for (Map<String, Object> record : learningList) {
                        if (Objects.equals(record.get("day"), day.getDay())) {
                            record.put("stage", STAGE_CGT);
                            record.put("date", day.getDate());
                            record.put("learningTopics", day.getLearningSession());
                            learningUpdated = true;
                            break;
                        }
                    }
                    if (!learningUpdated) {
                        Map<String, Object> learning = new LinkedHashMap<>();
                        learning.put("day", day.getDay());
                        learning.put("stage", STAGE_CGT);
                        learning.put("date", day.getDate());
                        learning.put("learningTopics", day.getLearningSession());
                        learningList.add(learning);
                    }
                    customer.setLearningSession(toJsonOrEmpty(learningList));

                    Map<String, Object> cgtInfo = new LinkedHashMap<>();
                    cgtInfo.put("cgtId", cgtId);
                    cgtInfo.put("groupId", requestObj.getGroupId());
                    cgtInfo.put("kendraId", requestObj.getKendraId());
                    customer.setCgtInfo(toJsonOrEmpty(cgtInfo));
                    customer.setCgtStatus(cgtStatus);

                    customer.setUpdatedTs(now);
                    customer.setUpdatedBy(userId);
                    toSaveMap.put(member.getCustomerId(), customer);

                } catch (Exception ex) {
                    logger.error("Unable to update Customer CGT Details for Customer Id : {}", member.getCustomerId(), ex);
                    throw new RuntimeException("Unable to update Customer CGT Details.", ex);
                }
            }
        }

        customerRepository.saveAll(new ArrayList<>(toSaveMap.values()));
        logger.info("Customer CGT Details Updated Successfully.");
    }

    /** Converts a value to its JSON string form; null or empty collections are stored as "". */
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

    /** Parses a stored JSON string back into a mutable list; blank/null is treated as an empty list. */
    private List<Map<String, Object>> fromJsonOrEmptyList(String json) {

        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing stored JSON value.", e);
        }
    }

    /** Builds/merges the day-wise annexure id string, e.g. "1-id#2-id"; only mandatory days onward count. */
    private String buildAnnexureId(String existingAnnexureId, List<CGTDayDetailsRequestFields> allDays) {

        Map<Integer, String> annexureMap = parseDayIdMap(existingAnnexureId);

        for (CGTDayDetailsRequestFields day : allDays) {
            if (day.getDay() >= cgtMandatoryDays) {
                if (day.getAnnexureId() != null && !day.getAnnexureId().trim().isEmpty()) {
                    annexureMap.put(day.getDay(), day.getAnnexureId());
                } else {
                    // Day resubmitted with no annexure id -> clear any stale value from a prior submission.
                    annexureMap.remove(day.getDay());
                }
            }
        }

        return buildDayIdString(annexureMap);
    }

    /** Builds/merges the day-wise group-photo id string; a group photo can be attached on any day. */
    private String buildGroupPhotoId(String existingGroupPhotoId, List<CGTDayDetailsRequestFields> allDays) {

        Map<Integer, String> photoMap = parseDayIdMap(existingGroupPhotoId);

        for (CGTDayDetailsRequestFields day : allDays) {
            if (day.getGroupPhotoId() != null && !day.getGroupPhotoId().trim().isEmpty()) {
                photoMap.put(day.getDay(), day.getGroupPhotoId());
            } else {
                // Day resubmitted with no group photo id -> clear any stale value from a prior submission.
                photoMap.remove(day.getDay());
            }
        }

        return buildDayIdString(photoMap);
    }

    /** Parses a "1-id#2-id" style string into a day-number-keyed map for merging with new days. */
    private Map<Integer, String> parseDayIdMap(String existing) {

        Map<Integer, String> dayIdMap = new LinkedHashMap<>();

        if (existing == null || existing.trim().isEmpty()) {
            return dayIdMap;
        }

        for (String entry : existing.split("#")) {
            if (entry == null || entry.trim().isEmpty()) {
                continue;
            }
            String[] parts = entry.split("-", 2);
            if (parts.length == 2) {
                try {
                    dayIdMap.put(Integer.parseInt(parts[0].trim()), parts[1].trim());
                } catch (NumberFormatException ex) {
                    logger.warn("Skipping malformed day-id entry : {}", entry);
                }
            }
        }

        return dayIdMap;
    }

    /** Renders a day-number-keyed id map back into "1-id#2-id" form, sorted by day. */
    private String buildDayIdString(Map<Integer, String> dayIdMap) {

        if (dayIdMap.isEmpty()) {
            return null;
        }

        return dayIdMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.joining("#"));
    }

    /** sub_stage is a TEXT column, so the request's list is stored as a JSON array string. */
    private String serializeSubStage(List<Map<String, Object>> subStage) {
        if (subStage == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(subStage);
        } catch (JsonProcessingException ex) {
            throw new CGTDateValidationException("Invalid subStage payload.");
        }
    }

    /** Returns sub_stage as a JSON structure, unwrapping values that were stringified more than once. */
    private Object parseSubStageForResponse(String subStage) {
        if (subStage == null || subStage.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(subStage, Object.class);
        } catch (Exception ex) {
            return subStage;
        }
    }

    /** Builds the JSON response after a save/schedule call, including any stage movement remark. */
    private String buildScheduleResponse(TbObCGTDetails cgtDetails, CGTDetailsRequestFields mergedObj, String stageMovementRemark) {
        try {
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("successMessage", "CGT Details Saved Successfully");
            responseMap.put("groupId", cgtDetails.getGroupId());
            responseMap.put("kendraId", cgtDetails.getKendraId());
            responseMap.put("status", cgtDetails.getStatus());
            responseMap.put("subStage", parseSubStageForResponse(cgtDetails.getSubStage()));
            responseMap.put("addCGTPayload", mergedObj.getAddCGTPayload());
            responseMap.put("conductCGTPayload", mergedObj.getConductCGTPayload());
            if (stageMovementRemark != null) {
                responseMap.put("stageMovementRemark", stageMovementRemark);
            }
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing response.", ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }

    /**
     * Advances eligible members (stage and wfstage both CGT) to BM Re-Interview.
     * Returns a remark on excluded members, or null if everyone advanced cleanly.
     */
    private String updateApplicationMasterStage(List<TbObApplicationMaster> applications, String userId) {

        logger.info("Updating Application Master Stage for all group members.");

        if (applications.isEmpty()) {
            logger.info("No Application Master records found for the mandatory day's memberLoanDetails.");
            return null;
        }

        List<TbObApplicationMaster> notAtCgtStageApplications = new ArrayList<>();
        List<TbObApplicationMaster> eligibleApplications = new ArrayList<>();

        for (TbObApplicationMaster application : applications) {
            if (STAGE_CGT.equalsIgnoreCase(application.getStage())
                    && WFStage.CGT.name().equalsIgnoreCase(application.getWfStage())) {
                eligibleApplications.add(application);
            } else {
                notAtCgtStageApplications.add(application);
            }
        }

        String exclusionSummary = notAtCgtStageApplications.stream()
                .map(application -> "Customer " + application.getCustomerId() + " (NOT_AT_CGT_STAGE)")
                .collect(Collectors.joining(", "));

        if (eligibleApplications.size() < cgtMinimumMembers) {
            String remark = "Only " + eligibleApplications.size() + " of " + applications.size()
                    + " members are eligible to proceed to BM Re-Interview (minimum " + cgtMinimumMembers
                    + " required)."
                    + (exclusionSummary.isEmpty() ? "" : " Excluded: " + exclusionSummary + ".");
            logger.info(remark);
            return remark;
        }

        LocalDateTime now = LocalDateTime.now();
        for (TbObApplicationMaster application : eligibleApplications) {
            application.setStage("BMQUEUE"); // 5 = BM_REINTERVIEW
            application.setWfStage("BMQUEUE"); // 5 = BM
            String ver = application.getVersion() == null ? "0":application.getVersion();
//            application.setStatus("BMQUEUE");
            application.setVersion(String.valueOf(Integer.parseInt(ver) + 1));
            application.setUpdatedBy(userId);
            application.setUpdatedTs(now);
        }

        notAtCgtStageApplications.forEach(application ->
                logger.info("Customer Id : {} has stage : {}, wfstage : {} (expected both CGT); excluded from BM Reinterview stage movement.",
                        application.getCustomerId(), application.getStage(), application.getWfStage()));

        applicationMasterRepository.saveAll(eligibleApplications);
        logger.info("Application Master Stage updated successfully for {} records.", eligibleApplications.size());

        return exclusionSummary.isEmpty() ? null
                : eligibleApplications.size() + " of " + applications.size()
                  + " members advanced to BM Re-Interview. Excluded: " + exclusionSummary + ".";
    }

    /** Computes the CGT progress marker: C1/C2/C3 while in progress, or COMPLETED once done. */
    private String computeCgtStatus(List<CGTDayDetailsRequestFields> allDays, boolean isCompleting) {
        return isCompleting
                ? "COMPLETED"
                : "C" + Math.min(getHighestCGTDay(allDays), cgtMandatoryDays);
    }

    /** Updates tb_ob_group's cgt_status, updatedBy, and updatedTs to match this submission. */
    private void updateGroupCgtDetails(CGTDetailsRequestFields requestObj, String cgtStatus, String userId) {

        logger.info("Updating tb_ob_group CGT details for Group Id : {}", requestObj.getGroupId());

        TbObGroup group = groupRepository.findByGroupId(requestObj.getGroupId())
                .orElseThrow(() -> new CGTDateValidationException(
                        "Group not found for Group Id : " + requestObj.getGroupId()));

        group.setCgtStatus(cgtStatus);
        group.setUpdatedBy(userId);
        group.setUpdatedTs(LocalDateTime.now());
        groupRepository.save(group);

        logger.info("Group Id : {} - cgt_status updated to {}.",
                requestObj.getGroupId(), cgtStatus);
    }

    /** Highest day number seen across all days in this submission; defaults to 1 if none. */
    private Integer getHighestCGTDay(List<CGTDayDetailsRequestFields> allDays) {

        return allDays.stream()
                .map(CGTDayDetailsRequestFields::getDay)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(1);
    }

    /** Read-only lookup of a group's recorded CGT progress by group id. */
    public Mono<Response> fetchCgtDayDetails(String groupId, Header header) {

        logger.info("Fetch CGT Details API Started.");

        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            if (groupId == null) {
                throw new CGTDateValidationException("Group Id is mandatory.");
            }

            logger.info("Fetching CGT Details for Group Id : {}", groupId);

            Optional<TbObCGTDetails> tbObCGTDetailsOpt = cgtRepository.findByGroupId(groupId);

            if (tbObCGTDetailsOpt.isPresent()) {
                responseBody.setResponseObj(buildFetchResponse(tbObCGTDetailsOpt.get()));
                CommonUtils.generateHeaderForSuccess(responseHeader);
            } else {
                logger.info("No CGT Details found for Group Id : {}.", groupId);
                CommonUtils.generateHeaderForNoResult(responseHeader);

                // Fallback to tb_ob_application_master disabled for now -- uncomment to restore
                // returning members currently sitting at CGT status when no tb_ob_cgt_details row exists.
                /*
                logger.info("No CGT Details found for Group Id : {} -- falling back to tb_ob_application_master.", groupId);

                List<TbObApplicationMaster> cgtStatusMembers = applicationMasterRepository
                        .findByGroupIdAndStatus(groupId, STATUS_CGT);

                if (cgtStatusMembers.isEmpty()) {
                    logger.info("No Application Master records at CGT status either, for Group Id : {}.", groupId);
                    CommonUtils.generateHeaderForNoResult(responseHeader);
                } else {
                    responseBody.setResponseObj(buildNoCgtDetailsResponse(cgtStatusMembers));
                    CommonUtils.generateHeaderForSuccess(responseHeader);
                }
                */
            }

        } catch (CGTDateValidationException ex) {
            logger.error("CGT Fetch Validation Failed.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());

        } catch (Exception ex) {
            logger.error("Exception while fetching CGT Details.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_OCCURED);
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /** Builds the fallback response from tb_ob_application_master when no CGT details row exists yet. */
    private String buildNoCgtDetailsResponse(List<TbObApplicationMaster> cgtStatusMembers) {
        try {
            List<Map<String, Object>> customers = new ArrayList<>();
            for (TbObApplicationMaster member : cgtStatusMembers) {
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("applicationId", member.getApplicationId());
                customer.put("customerId", member.getCustomerId());
                customer.put("customerName", member.getCustomerName());
                customer.put("status", member.getStatus());
                customer.put("wfstage", member.getWfStage());
                customers.add(customer);
            }

            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("groupId", cgtStatusMembers.get(0).getGroupId());
            responseMap.put("kendraId", cgtStatusMembers.get(0).getKendraId());
            responseMap.put("customers", customers);

            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing no-CGT-details fallback response.", ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }

    /** Builds the JSON response for a fetch, flagging any attendance count mismatch per day. */
    private String buildFetchResponse(TbObCGTDetails cgtDetails) {
        try {
            List<TbObApplicationMaster> groupMembers = applicationMasterRepository
                    .findByGroupIdAndStatusNot(cgtDetails.getGroupId(), ApplicationStatus.REJECTED.name());

            flagAttendanceMismatch(cgtDetails.getAddCGTPayload(), groupMembers);
            flagAttendanceMismatch(cgtDetails.getConductCGTPayload(), groupMembers);

            Map<String, Object> responseMap = objectMapper.convertValue(cgtDetails, new TypeReference<Map<String, Object>>() {});

            responseMap.put("subStage", parseSubStageForResponse(cgtDetails.getSubStage()));

            // RPCQUEUE/CRTQUEUE pending-member details disabled for now -- uncomment to restore
            // surfacing which members are stuck in per-member RPC review (CB_FAIL/RPC_PENDING).
            /*
            List<Map<String, Object>> rpcReviewPendingMembers = buildRpcReviewPendingMembers(groupMembers);
            if (!rpcReviewPendingMembers.isEmpty()) {
                responseMap.put("rpcReviewPendingMembers", rpcReviewPendingMembers);
            }
            */

            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing fetch response.", ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }

    /** Identifies group members missing from CGT because they're stuck in per-member RPC review. */
    private List<Map<String, Object>> buildRpcReviewPendingMembers(List<TbObApplicationMaster> groupMembers) {

        List<Map<String, Object>> pendingMembers = new ArrayList<>();

        for (TbObApplicationMaster member : groupMembers) {

            String reason;
            if (WFStage.CRTQUEUE.name().equalsIgnoreCase(member.getWfStage())) {
                reason = "CB_FAIL";
            } else if (WFStage.RPCQUEUE.name().equalsIgnoreCase(member.getWfStage())) {
                reason = "RPC_QUEUE";
            } else {
                continue;
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("customerId", member.getCustomerId());
            entry.put("customerName", member.getCustomerName());
            entry.put("applicationId", member.getApplicationId());
            entry.put("reason", reason);
            pendingMembers.add(entry);
        }

        return pendingMembers;
    }

    /**
     * Flags a day if its captured member count doesn't match the group's current member count.
     * Adds a missingCandidates list rather than guessing attendance; nothing is persisted here.
     */
    @SuppressWarnings("unchecked")
    private void flagAttendanceMismatch(List<Object> dayPayloads, List<TbObApplicationMaster> groupMembers) {

        if (dayPayloads == null) {
            return;
        }

        for (Object dayObj : dayPayloads) {

            if (!(dayObj instanceof Map)) {
                continue;
            }
            Map<String, Object> day = (Map<String, Object>) dayObj;

            Object memberDetailsObj = day.get("memberDetails");
            List<Map<String, Object>> memberDetails = memberDetailsObj instanceof List
                    ? (List<Map<String, Object>>) memberDetailsObj
                    : new ArrayList<>();

            if (memberDetails.size() != groupMembers.size()) {
                day.put("attendanceMismatch", "misMatch: attendance member count (" + memberDetails.size()
                        + ") does not match group member count (" + groupMembers.size() + ").");

                Set<String> capturedCustomerIds = memberDetails.stream()
                        .map(record -> Objects.toString(record.get("customerId"), null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                List<Map<String, Object>> missingCandidates = groupMembers.stream()
                        .filter(member -> !capturedCustomerIds.contains(member.getCustomerId()))
                        .map(member -> {
                            Map<String, Object> candidate = new LinkedHashMap<>();
                            candidate.put("customerId", member.getCustomerId());
                            candidate.put("customerName", member.getCustomerName());
                            return candidate;
                        })
                        .collect(Collectors.toList());

                if (!missingCandidates.isEmpty()) {
                    day.put("missingCandidates", missingCandidates);
                }
            }
        }
    }
}
