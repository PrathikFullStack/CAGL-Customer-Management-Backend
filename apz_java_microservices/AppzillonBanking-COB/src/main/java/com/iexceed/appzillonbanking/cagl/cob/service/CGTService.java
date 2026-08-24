package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.enums.WFStage;
import com.iexceed.appzillonbanking.cagl.cob.exception.CGTDateValidationException;
import com.iexceed.appzillonbanking.cagl.cob.payload.CGTDayDetailsRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.CGTDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.CGTDetailsRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.CGTMemberDetailsRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCGTDetailsRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustOthersRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLoanRepository;
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
    private TbObCustOthersRepository custOthersRepository;

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

    // Deliberately instance (not static) fields -- Spring's @Value injection silently no-ops on
    // static fields, which previously left these permanently at 0 regardless of what
    // application.properties said, and quietly disabled every check that used them.
    @Value("${onboarding.CGTService.cgt-mandatory-days:3}")
    private int cgtMandatoryDays;

    @Value("${onboarding.CGTService.cgt-minimum-members:5}")
    private int cgtMinimumMembers;

    private static final String CGT_ID_SEQUENCE = "seq_ob_cgt_schedule_id";
    private static final String CUST_OTHER_ID_SEQUENCE = "seq_ob_cust_others_id";

    public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";
    public static final String EXCEPTION_OCCURED = "Exception occurred";

    private static final Logger logger = LogManager.getLogger(CGTService.class);

    /**
     * Single entry point for both the real CGT submit and the KM's "Save" (draft) action --
     * distinguished by {@code requestObj.isDraft}:
     * <ul>
     *   <li><b>isDraft = true</b> (Save button): no validation at all. Only
     *       {@code tb_ob_cgt_details} is written, exactly as sent, however incomplete. A new
     *       record starts at status {@code PENDING}; an existing record's status is left exactly
     *       as it was, so a draft save can never mark a day complete or touch any other table.</li>
     *   <li><b>isDraft = false/absent</b> (real submit -- Mark Attendance, Complete training
     *       topics, End CGT for Day X, End CGT): full validation runs, and every table listed
     *       in the class-level doc is updated. Setting {@code endCGTFlag = true} additionally
     *       enforces the Day-3 mandatory checks and advances the application stage.</li>
     * </ul>
     * {@code isDraft} and {@code endCGTFlag} are mutually exclusive -- a draft save can never
     * also be the call that ends CGT, so sending both as {@code true} fails validation outright.
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
                validateMinimumMembers(allDays);
                validateLearningSession(allDays);

                if (Boolean.TRUE.equals(requestObj.getEndCGTFlag())) {
                    validateWithMandatoryDays(requestObj);
                    validateAnnexureAndLoanCapture(allDays);
                }

                if (tbObCGTDetailsOpt.isPresent()) {
                    cgtDetails = tbObCGTDetailsOpt.get();
                    updateExistingCGT(cgtDetails, requestObj, allDays, request.getUserId(), requestObj.getStatus(), requestObj.getSubStage());
                } else {
                    cgtDetails = createNewCGT(requestObj, allDays, request.getUserId(), requestObj.getStatus(), requestObj.getSubStage());
                }
                cgtRepository.save(cgtDetails);

                // Fetched once here and reused by both updateCustomerCGTDetails and
                // updateApplicationMasterStage, instead of each independently re-querying
                // tb_ob_application_master for the same customer IDs.
                List<String> customerIds = extractCustomerIds(allDays);
                List<TbObApplicationMaster> applications = customerIds.isEmpty()
                        ? Collections.emptyList()
                        : applicationMasterRepository.findByCustomerIdIn(customerIds);

                updateCustomerCGTDetails(requestObj, allDays, customerIds, applications, request.getUserId(), cgtDetails.getCgtId());

                if (Boolean.TRUE.equals(requestObj.getEndCGTFlag())) {
                    stageMovementRemark = updateApplicationMasterStage(applications, request.getUserId());
                }

                updateGroupCgtDetails(requestObj, allDays, request.getUserId(), Boolean.TRUE.equals(requestObj.getEndCGTFlag()));
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

    /**
     * Flattens {@code conductCGTPayload} and {@code addCGTPayload} into one list, computed once
     * per request and threaded through every method below that needs "all days" -- avoids the
     * same two-payload merge being repeated from scratch in nine different places.
     */
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

    /** Distinct, non-null customer IDs referenced anywhere across all days in this submission. */
    private List<String> extractCustomerIds(List<CGTDayDetailsRequestFields> allDays) {
        return allDays.stream()
                .filter(day -> day.getMemberDetails() != null)
                .flatMap(day -> day.getMemberDetails().stream())
                .map(CGTMemberDetailsRequestFields::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Every day submitted must have at least {@code cgtMinimumMembers} entries in
     * {@code memberDetails} -- CGT can't be conducted or recorded for a group that small on
     * that day.
     */
    private void validateMinimumMembers(List<CGTDayDetailsRequestFields> allDays) {

        logger.info("Validating Minimum Members per CGT Day.");

        for (CGTDayDetailsRequestFields day : allDays) {
            List<CGTMemberDetailsRequestFields> members = day.getMemberDetails();
            if (members == null || members.size() < cgtMinimumMembers) {
                throw new CGTDateValidationException(
                        "Minimum " + cgtMinimumMembers + " members are mandatory for Day-" + day.getDay() + ".");
            }
        }

        logger.info("Minimum Members Validation Completed.");
    }

    /**
     * Every day submitted must record what topics were actually covered -- a day with no
     * {@code learningSession} entries is treated as not having been conducted at all.
     */
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

    /**
     * Runs only when the KM is ending CGT (endCGTFlag=true): all {@code cgtMandatoryDays} days
     * (default 3) must be accounted for, whether conducted fully offline ({@code addCGTPayload}),
     * fully online ({@code conductCGTPayload}), or a mix of both. When mixed, any offline day must
     * come strictly after the last online day -- offline entry is only meant for "extra" days
     * added on top of what was already conducted online, never to backfill an online gap. This
     * one needs the two payloads kept separate (not the merged {@code allDays}) precisely because
     * it distinguishes offline from online.
     */
    private void validateWithMandatoryDays(CGTDetailsRequestFields requestObj) {

        logger.info("Validating Mandatory CGT Days.");

        List<CGTDayDetailsRequestFields> offlineDays = requestObj.getAddCGTPayload();
        List<CGTDayDetailsRequestFields> onlineDays = requestObj.getConductCGTPayload();

        boolean hasOffline = offlineDays != null && !offlineDays.isEmpty();
        boolean hasOnline = onlineDays != null && !onlineDays.isEmpty();

        if (hasOffline && !hasOnline) {
            if (offlineDays.size() < cgtMandatoryDays) {
                throw new CGTDateValidationException(
                        "Minimum " + cgtMandatoryDays + " CGT days are mandatory.");
            }

            Set<Integer> daySet = offlineDays.stream()
                    .map(CGTDayDetailsRequestFields::getDay)
                    .collect(Collectors.toSet());

            for (int i = 1; i <= cgtMandatoryDays; i++) {
                if (!daySet.contains(i)) {
                    throw new CGTDateValidationException(
                            "Day-" + i + " is mandatory.");
                }
            }
            return;
        }

        if (hasOnline && !hasOffline) {
            if (onlineDays.size() < cgtMandatoryDays) {
                throw new CGTDateValidationException(
                        "Minimum " + cgtMandatoryDays + " CGT days are mandatory.");
            }
            Set<Integer> daySet = onlineDays.stream()
                    .map(CGTDayDetailsRequestFields::getDay)
                    .collect(Collectors.toSet());

            for (int i = 1; i <= cgtMandatoryDays; i++) {
                if (!daySet.contains(i)) {
                    throw new CGTDateValidationException(
                            "Day-" + i + " is mandatory.");
                }
            }
            return;
        }

        if (hasOffline && hasOnline) {
            int lastOnlineDay = onlineDays.stream()
                    .mapToInt(CGTDayDetailsRequestFields::getDay)
                    .max()
                    .orElse(0);

            for (CGTDayDetailsRequestFields day : offlineDays) {
                if (day.getDay() <= lastOnlineDay) {
                    throw new CGTDateValidationException(
                            "Offline CGT can contain only additional days after Day-"
                                    + lastOnlineDay);
                }
            }

            Set<Integer> combinedDaySet = new HashSet<>();
            onlineDays.forEach(d -> combinedDaySet.add(d.getDay()));
            offlineDays.forEach(d -> combinedDaySet.add(d.getDay()));

            if (combinedDaySet.size() < cgtMandatoryDays) {
                throw new CGTDateValidationException(
                        "Minimum " + cgtMandatoryDays + " CGT days are mandatory.");
            }
            for (int i = 1; i <= cgtMandatoryDays; i++) {
                if (!combinedDaySet.contains(i)) {
                    throw new CGTDateValidationException(
                            "Day-" + i + " is mandatory.");
                }
            }
        }
    }

    /**
     * Hands a defensive copy of {@code allDays} to {@link #validateCGTSchedule} (which sorts its
     * input in place) so the shared {@code allDays} list's original order is never mutated for
     * whichever other method reads it next in the same request.
     */
    private void validateScheduleDates(List<CGTDayDetailsRequestFields> allDays) {
        logger.info("Validating CGT Dates.");

        if (!allDays.isEmpty()) {
            validateCGTSchedule(new ArrayList<>(allDays));
        }

        logger.info("CGT Date Validation Completed.");
    }

    /**
     * Enforces day ordering regardless of which payload each day came from: Day-1 must exist,
     * day numbers can't skip, each day must fall at least one calendar day after the previous
     * one, and the last day recorded can't be in the future.
     */
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
     * Runs only when ending CGT (endCGTFlag=true): the final mandatory day must have a signed
     * annexure attached and loan details marked as captured -- CGT can't be closed without both.
     * {@link #validateLoanDetailsCaptured} then cross-checks that "loan details captured" claim
     * against {@code tb_ob_loan} rather than trusting the client's boolean at face value.
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

    /**
     * A day can claim {@code loanDetailsCapture=true} without a loan actually having been
     * captured, so this verifies every member on that day has a matching row in
     * {@code tb_ob_loan} and fails with the specific customer IDs that don't.
     */
    private void validateLoanDetailsCaptured(CGTDayDetailsRequestFields day) {

        if (day.getMemberDetails() == null || day.getMemberDetails().isEmpty()) {
            return;
        }

        List<String> customerIds = day.getMemberDetails().stream()
                .map(CGTMemberDetailsRequestFields::getCustomerId)
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
                .toList();

        if (!missingLoanDetails.isEmpty()) {
//            throw new CGTDateValidationException(
//                    "Loan details not found in tb_ob_loan for Customer Id(s) : " + missingLoanDetails
//                            + " on Day-" + cgtMandatoryDays + ".");
            logger.warn("Loan details not found in tb_ob_loan for Customer Id(s) : {}", missingLoanDetails);
        }
    }

    /** First-ever submission for this group: builds a fresh {@code tb_ob_cgt_details} row. */
    private TbObCGTDetails createNewCGT(CGTDetailsRequestFields mergedObj, List<CGTDayDetailsRequestFields> allDays, String userId, String status, String subStage) {

        logger.info("Creating New CGT Details.");

        TbObCGTDetails cgtDetails = TbObCGTDetails.builder()
                .cgtId(nextCgtId())
                .groupId(mergedObj.getGroupId())
                .kendraId(mergedObj.getKendraId())
                .addCGTPayload(mergedObj.getAddCGTPayload() != null ? new ArrayList<>(mergedObj.getAddCGTPayload()) : null)
                .conductCGTPayload(mergedObj.getConductCGTPayload() != null ? new ArrayList<>(mergedObj.getConductCGTPayload()) : null)
                .status(status != null ? status : "PENDING")
                .subStage(subStage)
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

    /** Draws the next {@code cgt_id} straight from the Postgres sequence, bypassing the JPA/Hibernate id generator. */
    private String nextCgtId() {
        return sequenceUtil.nextValueAsString(CGT_ID_SEQUENCE);
    }

    /**
     * A later submission for a group that already has a {@code tb_ob_cgt_details} row: day
     * payloads are replaced wholesale with whatever was just sent (the caller is expected to
     * send the full merged set of days each time, not a delta), while annexure/group-photo are
     * merged additively across days via {@link #buildAnnexureId}/{@link #buildGroupPhotoId}.
     */
    private void updateExistingCGT(TbObCGTDetails cgtDetails, CGTDetailsRequestFields mergedObj, List<CGTDayDetailsRequestFields> allDays, String userId, String status, String subStage) {

        logger.info("Updating Existing CGT Details.");

        cgtDetails.setAddCGTPayload(mergedObj.getAddCGTPayload() != null ? new ArrayList<>(mergedObj.getAddCGTPayload()) : null);
        cgtDetails.setConductCGTPayload(mergedObj.getConductCGTPayload() != null ? new ArrayList<>(mergedObj.getConductCGTPayload()) : null);
        cgtDetails.setStatus(status != null ? status : cgtDetails.getStatus());
        cgtDetails.setSubStage(subStage);
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
     * Mirrors this submission's attendance and learning-topics into each member's own
     * {@code tb_ob_cust_others} row, keyed by day number so re-submitting the same day updates
     * that day's entry in place instead of duplicating it. This builds up incrementally across
     * every submission (not just the final one), so a member's attendance history is always
     * current regardless of how many times a day gets re-saved. {@code customerIds}/{@code applications}
     * are passed in already fetched from {@link #conductCGT} rather than re-derived/re-queried here.
     */
    private void updateCustomerCGTDetails(CGTDetailsRequestFields requestObj, List<CGTDayDetailsRequestFields> allDays,
                                          List<String> customerIds, List<TbObApplicationMaster> applications,
                                          String userId, String cgtId) {

        logger.info("Updating Customer CGT Details.");

        if (allDays.isEmpty()) {
            logger.info("No CGT Day Details found.");
            return;
        }

        if (customerIds.isEmpty()) {
            logger.info("No members found in CGT Day Details.");
            return;
        }

        Map<String, TbObCustOthers> existingMap = custOthersRepository
                .findByCustomerIdIn(customerIds)
                .stream()
                .collect(Collectors.toMap(TbObCustOthers::getCustomerId, c -> c));

        Map<String, String> customerIdToApplicationId = applications.stream()
                .collect(Collectors.toMap(TbObApplicationMaster::getCustomerId,
                        TbObApplicationMaster::getApplicationId, (existing, duplicate) -> existing));

        LocalDateTime now = LocalDateTime.now();
        Map<String, TbObCustOthers> toSaveMap = new LinkedHashMap<>();

        for (CGTDayDetailsRequestFields day : allDays) {

            if (day.getMemberDetails() == null || day.getMemberDetails().isEmpty()) {
                continue;
            }

            for (CGTMemberDetailsRequestFields member : day.getMemberDetails()) {

                if (member.getCustomerId() == null) {
                    logger.warn("Skipping member with null Customer Id for Day-{}.", day.getDay());
                    continue;
                }

                try {
                    TbObCustOthers custOthers = existingMap.get(member.getCustomerId());

                    if (custOthers == null) {
                        custOthers = new TbObCustOthers();
                        custOthers.setCustOtherId(sequenceUtil.nextValueAsString(CUST_OTHER_ID_SEQUENCE));
                        custOthers.setCustomerId(member.getCustomerId());
                        custOthers.setApplicationId(customerIdToApplicationId.get(member.getCustomerId()));
                        custOthers.setCreatedTs(now);
                        existingMap.put(member.getCustomerId(), custOthers);
                    }

//                    List<Map<String, Object>> attendanceList = custOthers.getAttendance() != null
//                            ? custOthers.getAttendance()
//                            : new ArrayList<>();
                    List<Map<String, Object>> attendanceList = fromJsonOrEmptyList(custOthers.getAttendance());

                    boolean attendanceUpdated = false;
                    for (Map<String, Object> record : attendanceList) {
                        if (Objects.equals(record.get("day"), day.getDay())) {
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
                        attendance.put("date", day.getDate());
                        attendance.put("present", member.getPresent());
                        attendance.put("markedBy", userId);
                        attendanceList.add(attendance);
                    }
//                    custOthers.setAttendance(attendanceList);
                    custOthers.setAttendance(toJsonOrEmpty(attendanceList));

//                    List<Map<String, Object>> learningList = custOthers.getLearningSession() != null
//                            ? custOthers.getLearningSession()
//                            : new ArrayList<>();

                    List<Map<String, Object>> learningList = fromJsonOrEmptyList(custOthers.getLearningSession());

                    boolean learningUpdated = false;
                    for (Map<String, Object> record : learningList) {
                        if (Objects.equals(record.get("day"), day.getDay())) {
                            record.put("date", day.getDate());
                            record.put("learningTopics", day.getLearningSession());
                            learningUpdated = true;
                            break;
                        }
                    }
                    if (!learningUpdated) {
                        Map<String, Object> learning = new LinkedHashMap<>();
                        learning.put("day", day.getDay());
                        learning.put("date", day.getDate());
                        learning.put("learningTopics", day.getLearningSession());
                        learningList.add(learning);
                    }
//                    custOthers.setLearningSession(learningList);
                    custOthers.setLearningSession(toJsonOrEmpty(learningList));

                    Map<String, Object> cgtInfo = new LinkedHashMap<>();
                    cgtInfo.put("cgtId", cgtId);
                    cgtInfo.put("groupId", requestObj.getGroupId());
                    cgtInfo.put("kendraId", requestObj.getKendraId());
//                    custOthers.setCgtInfo(cgtInfo);
                    custOthers.setCgtInfo(toJsonOrEmpty(cgtInfo));

                    custOthers.setUpdatedTs(now);
                    custOthers.setUpdatedBy(userId);
                    toSaveMap.put(member.getCustomerId(), custOthers);

                } catch (Exception ex) {
                    logger.error("Unable to update Customer CGT Details for Customer Id : {}", member.getCustomerId(), ex);
                    throw new RuntimeException("Unable to update Customer CGT Details.", ex);
                }
            }
        }

        custOthersRepository.saveAll(new ArrayList<>(toSaveMap.values()));
        logger.info("Customer CGT Details Updated Successfully.");
    }

    /**
     * Serializes any {@code tb_ob_cust_others} sub-payload (attendance/learningSession/cgtInfo)
     * to its JSON string form for storage in the now-TEXT columns. A null value or empty
     * collection is stored as "" rather than the literal "null"/"[]".
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

    /**
     * Deserializes the JSON string persisted in {@code attendance}/{@code learningSession}
     * (now TEXT columns) back into a mutable {@code List<Map<String, Object>>} so the day-wise
     * merge logic can update it in place, same as before these columns moved off native JSON
     * types. A null/blank value (new member, or column not yet populated) is treated as an
     * empty, mutable list rather than an error.
     */
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

    /**
     * Builds/merges the day-wise annexure id string stored on {@code tb_ob_cgt_details}, e.g.
     * {@code "1-<annexureId>#2-<annexureId>"}. Only days at or beyond {@code cgtMandatoryDays}
     * contribute an entry (earlier days don't require an annexure). Since {@code allDays} is the
     * full day list resent on every submission, whatever value a day carries in the current
     * request wins for that day -- overwriting it if non-empty, clearing it if null/empty --
     * while days entirely absent from the current submission keep whatever was persisted before.
     */
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

    /**
     * Builds/merges the day-wise group-photo id string on {@code tb_ob_cgt_details}, same merge
     * semantics as {@link #buildAnnexureId} but with no mandatory-day restriction -- a group
     * photo can be attached on any day.
     */
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

    /**
     * Parses a {@code "1-<id>#2-<id>"} style string back into a day-number-keyed map so it can be
     * merged with newly submitted days. The split on {@code "-"} is limited to 2 parts since the
     * id itself (e.g. a UUID) may contain hyphens.
     */
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

    /** Renders a day-number-keyed id map back into {@code "1-<id>#2-<id>"} form, sorted by day. */
    private String buildDayIdString(Map<Integer, String> dayIdMap) {

        if (dayIdMap.isEmpty()) {
            return null;
        }

        return dayIdMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.joining("#"));
    }

    /**
     * Builds the JSON response body returned to the caller after a save/schedule call.
     * {@code stageMovementRemark} is only non-null when {@link #updateApplicationMasterStage} had
     * something to report (members held back from BM Re-Interview, or excluded from it) --
     * absent from the response entirely otherwise.
     */
    private String buildScheduleResponse(TbObCGTDetails cgtDetails, CGTDetailsRequestFields mergedObj, String stageMovementRemark) {
        try {
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("successMessage", "CGT Details Saved Successfully");
            responseMap.put("groupId", cgtDetails.getGroupId());
            responseMap.put("kendraId", cgtDetails.getKendraId());
            responseMap.put("status", cgtDetails.getStatus());
            responseMap.put("subStage", cgtDetails.getSubStage());
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
     * Only called when CGT is being ended (endCGTFlag=true), with {@code applications} already
     * fetched once by {@link #conductCGT} and shared with {@link #updateCustomerCGTDetails} --
     * no second query against {@code tb_ob_application_master} happens here. Advances every
     * eligible member's application to stage 5 (BM Re-Interview) -- but "eligible" excludes
     * anyone already {@code REJECTED}, anyone still parked in per-member RPC review with
     * {@code wfstage = RPCQUEUE} (RPC review pending) or {@code wfstage = CRTQUEUE} (Credit
     * Bureau check failed during RPC review -- "CB Fail"), e.g.:
     * <pre>
     *   select * from tb_ob_application_master where wfstage = 'CRTQUEUE' and status = 'CGT'; -- CB Fail during RPC review
     *   select * from tb_ob_application_master where wfstage = 'RPCQUEUE' and status = 'CGT'; -- RPC review pending
     * </pre>
     * A rejected application must never be pulled back into the pipeline, and none of these three
     * categories may inflate the quorum count. The whole group is held back from advancing (not
     * just the excluded members) if fewer than {@code cgtMinimumMembers} would be left to actually
     * move forward -- in that case nothing here is persisted, so the excluded members stay exactly
     * as they were (RPCQUEUE/CRTQUEUE/REJECTED) for whatever process is meant to resolve them next.
     * {@code tb_ob_cgt_details}/{@code tb_ob_group} still get marked COMPLETED for the group either
     * way -- only the application-stage movement is conditional on this check.
     *
     * @return a human-readable remark describing which members were excluded (REJECTED/CB_FAIL/
     *         RPC_QUEUE) and why the group was or wasn't advanced -- {@code null} when every
     *         member advanced cleanly with no exclusions to report.
     */
    private String updateApplicationMasterStage(List<TbObApplicationMaster> applications, String userId) {

        logger.info("Updating Application Master Stage for all group members.");

        if (applications.isEmpty()) {
            logger.info("No Application Master records found for the given customer IDs.");
            return null;
        }

        List<TbObApplicationMaster> rejectedApplications = new ArrayList<>();
        List<TbObApplicationMaster> cbFailApplications = new ArrayList<>();
        List<TbObApplicationMaster> rpcPendingApplications = new ArrayList<>();
        List<TbObApplicationMaster> eligibleApplications = new ArrayList<>();

        for (TbObApplicationMaster application : applications) {
            if (ApplicationStatus.REJECTED.name().equalsIgnoreCase(application.getStatus())) {
                rejectedApplications.add(application);
            } else if (WFStage.CRTQUEUE.name().equalsIgnoreCase(application.getWfStage())) {
                cbFailApplications.add(application);
            } else if (WFStage.RPCQUEUE.name().equalsIgnoreCase(application.getWfStage())) {
                rpcPendingApplications.add(application);
            } else {
                eligibleApplications.add(application);
            }
        }

        String exclusionSummary = buildExclusionSummary(rejectedApplications, cbFailApplications, rpcPendingApplications);

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
            application.setStage("5"); // 5 = BM_REINTERVIEW
            application.setWfStage(WFStage.BMQUEUE.name()); // 5 = BM
            application.setVersion(String.valueOf(Integer.parseInt(application.getVersion()) + 1));
            application.setStatus(ApplicationStatus.BMQUEUE.name());
            application.setUpdatedBy(userId);
            application.setUpdatedTs(now);
        }

        cbFailApplications.forEach(application ->
                logger.info("Customer Id : {} is CB_FAIL (wfstage=CRTQUEUE) during RPC review; not advancing to BM Reinterview stage.",
                        application.getCustomerId()));

        rpcPendingApplications.forEach(application ->
                logger.info("Customer Id : {} is still RPC_QUEUE; not advancing to BM Reinterview stage.",
                        application.getCustomerId()));

        rejectedApplications.forEach(application ->
                logger.info("Customer Id : {} is REJECTED; excluded from BM Reinterview stage movement.",
                        application.getCustomerId()));

        applicationMasterRepository.saveAll(eligibleApplications);
        logger.info("Application Master Stage updated successfully for {} records.", eligibleApplications.size());

        return exclusionSummary.isEmpty() ? null
                : eligibleApplications.size() + " of " + applications.size()
                  + " members advanced to BM Re-Interview. Excluded: " + exclusionSummary + ".";
    }

    /** Formats each excluded member as {@code "Customer <id> (REJECTED|RPC_QUEUE)"}, comma-joined. */
    private String buildExclusionSummary(List<TbObApplicationMaster> rejectedApplications,
                                         List<TbObApplicationMaster> cbFailApplications,
                                         List<TbObApplicationMaster> rpcPendingApplications) {

        List<String> parts = new ArrayList<>();
        rejectedApplications.forEach(application ->
                parts.add("Customer " + application.getCustomerId() + " (REJECTED)"));
        cbFailApplications.forEach(application ->
                parts.add("Customer " + application.getCustomerId() + " (CB_FAIL)"));
        rpcPendingApplications.forEach(application ->
                parts.add("Customer " + application.getCustomerId() + " (RPC_QUEUE)"));
        return String.join(", ", parts);
    }

    /**
     * Refreshes {@code tb_ob_group.cgt_status} on every real submission: {@code C1}/{@code C2}/
     * {@code C3} while days are being conducted (capped at {@code cgtMandatoryDays} -- extra days
     * beyond that stay at {@code C<cgtMandatoryDays>} until actually completed), or
     * {@code COMPLETED} once the KM ends CGT. Also stamps {@code last_activity_ts} so the group
     * doesn't look inactive.
     */
    private void updateGroupCgtDetails(CGTDetailsRequestFields requestObj, List<CGTDayDetailsRequestFields> allDays, String userId, boolean endCGT) {

        logger.info("Updating tb_ob_group CGT details for Group Id : {}", requestObj.getGroupId());

        TbObGroup group = groupRepository.findByGroupId(requestObj.getGroupId())
                .orElseThrow(() -> new CGTDateValidationException(
                        "Group not found for Group Id : " + requestObj.getGroupId()));

        String cgtStatus = endCGT
                ? "COMPLETED"
                : "C" + Math.min(getHighestCGTDay(allDays), cgtMandatoryDays);

        group.setCgtStatus(cgtStatus);
        group.setUpdatedBy(userId);
        group.setUpdatedTs(LocalDateTime.now());
        groupRepository.save(group);

        logger.info("Group Id : {} - cgt_status updated to {}.", requestObj.getGroupId(), cgtStatus);
    }

    /** Highest day number seen across all days in this submission; defaults to 1 if none. */
    private Integer getHighestCGTDay(List<CGTDayDetailsRequestFields> allDays) {

        return allDays.stream()
                .map(CGTDayDetailsRequestFields::getDay)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(1);
    }

    /**
     * Read-only lookup of a group's recorded CGT progress. When {@code tb_ob_cgt_details} has
     * nothing saved yet for the group (CGT hasn't actually started), falls back to
     * {@code tb_ob_application_master} -- see {@link #buildNoCgtDetailsResponse} -- so the caller
     * still gets back who's waiting to be taken through CGT, instead of just an empty result.
     * Only when even that fallback finds nothing (no members recorded against the group at all)
     * is a genuine "no result" response returned.
     */
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
                logger.info("No CGT Details found for Group Id : {} -- falling back to tb_ob_application_master.", groupId);

                List<TbObApplicationMaster> cgtStatusMembers = applicationMasterRepository
                        .findByGroupIdAndStatus(groupId, ApplicationStatus.CGT.name());

                if (cgtStatusMembers.isEmpty()) {
                    logger.info("No Application Master records at CGT status either, for Group Id : {}.", groupId);
                    CommonUtils.generateHeaderForNoResult(responseHeader);
                } else {
                    responseBody.setResponseObj(buildNoCgtDetailsResponse(cgtStatusMembers));
                    CommonUtils.generateHeaderForSuccess(responseHeader);
                }
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

    /**
     * Builds the fallback response used when {@code tb_ob_cgt_details} has no row yet for the
     * group -- sourced entirely from {@code tb_ob_application_master} members currently at
     * {@code status = 'CGT'}, i.e.:
     * <pre>
     *   select * from tb_ob_application_master where group_id = ? and status = 'CGT';
     * </pre>
     * {@code groupId}/{@code kendraId} are the same for every member of a group, so they're lifted
     * to the top level once rather than repeated per customer; only what actually varies per
     * member (applicationId/customerId/customerName/status/wfstage) goes in the {@code customers}
     * array. {@code status} is 'CGT' for every entry here (that's the query filter), but
     * {@code wfstage} still varies per member -- e.g. plain 'CGT', or 'RPCQUEUE'/'CRTQUEUE' for
     * someone parked in per-member RPC review -- so both are returned rather than just one.
     */
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

    /**
     * Builds the JSON response for a fetch, after flagging any day whose captured
     * {@code memberDetails} count doesn't match the group's current (non-rejected) member count --
     * see {@link #flagAttendanceMismatch} for why that gap can happen and why it's flagged rather
     * than silently patched. Also surfaces which of those members are missing from CGT because
     * they're stuck in per-member RPC review -- see {@link #buildRpcReviewPendingMembers}.
     */
    private String buildFetchResponse(TbObCGTDetails cgtDetails) {

        try {
            List<TbObApplicationMaster> groupMembers = applicationMasterRepository
                    .findByGroupIdAndStatusNot(cgtDetails.getGroupId(), ApplicationStatus.REJECTED.name());

            flagAttendanceMismatch(cgtDetails.getAddCGTPayload(), groupMembers);
            flagAttendanceMismatch(cgtDetails.getConductCGTPayload(), groupMembers);

            Map<String, Object> responseMap = objectMapper.convertValue(cgtDetails, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> rpcReviewPendingMembers = buildRpcReviewPendingMembers(groupMembers);
            if (!rpcReviewPendingMembers.isEmpty()) {
                responseMap.put("rpcReviewPendingMembers", rpcReviewPendingMembers);
            }
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception ex) {
            logger.error("Error while preparing fetch response.", ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }

    /**
     * Identifies group members who are missing from CGT's own {@code memberDetails} because
     * they're parked in per-member RPC review rather than off {@code tb_ob_cgt_details} at all --
     * looked up purely off {@code tb_ob_application_master.wfstage}, mirroring the same two
     * categories {@link #updateApplicationMasterStage} excludes from stage movement:
     * <pre>
     *   select * from tb_ob_application_master where wfstage = 'CRTQUEUE' and status = 'CGT'; -- CB Fail during RPC review
     *   select * from tb_ob_application_master where wfstage = 'RPCQUEUE' and status = 'CGT'; -- RPC review pending
     * </pre>
     * Returns just enough to identify each member (customerId/customerName/applicationId) plus why
     * they're missing, so the caller doesn't need a separate query against
     * {@code tb_ob_application_master} to explain the gap flagged by {@link #flagAttendanceMismatch}.
     */
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
     * {@code tb_ob_cgt_details} only stores whichever members the KM actually captured
     * attendance for on a given day. If a member joined the group later (or was skipped), that
     * day's {@code memberDetails} count won't match the group's current (non-rejected) member
     * count. Rather than fabricating a {@code present=null} row for whoever's missing -- which
     * would misrepresent something that was never actually recorded -- each affected day just
     * gets an {@code attendanceMismatch} remark added, plus a {@code missingCandidates} list
     * (customerId/customerName, from {@code groupMembers}) naming exactly who wasn't captured, so
     * the caller knows the counts disagree, who's responsible for the gap, and can decide what to
     * do about it. A member already captured with {@code present=null} (genuinely marked but not
     * yet confirmed present/absent) is left exactly as-is; this only reacts to members missing
     * entirely. This only affects the response -- nothing is written back to {@code tb_ob_cgt_details}.
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
