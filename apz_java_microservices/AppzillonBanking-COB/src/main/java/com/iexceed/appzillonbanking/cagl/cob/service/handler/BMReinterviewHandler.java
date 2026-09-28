package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocument;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.exception.BMReinterviewValidationException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObBMReInterviewRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustomerRepository;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObBMReInterview;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLoanRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditService;
import com.iexceed.appzillonbanking.cagl.cob.service.BMReinterviewService;
import com.iexceed.appzillonbanking.cagl.cob.utils.SequenceUtil;

import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Owns every read/write against {@code tb_ob_bm_reinterview}, {@code tb_ob_customer} (KYC) and
 * {@code tb_ob_application_master} that a BM Reinterview submission triggers.
 * <p>
 * This is a separate Spring bean -- not a private method on {@link BMReinterviewService} -- purely
 * so {@link #process} runs inside a real transactional proxy: Spring's {@code @Transactional}
 * advice only kicks in on calls that cross a bean boundary, so invoking it from the facade (a
 * different bean) guarantees that a failure partway through (e.g. the reinterview record saves
 * fine but the KYC update then throws) rolls the whole submission back, instead of the facade's
 * own try/catch silently swallowing the exception into a "failure" response while the earlier
 * writes stay committed.
 */
@Service
@RequiredArgsConstructor
public class BMReinterviewHandler {

    private static final Logger logger = LogManager.getLogger(BMReinterviewHandler.class);

    private static final String BM = "BM";
    private static final String DOC_VERIFICATION_SUB_STAGE = "1.1";
    private static final String KNOWLEDGE_TEST_SUB_STAGE = "1.2";
    private static final String HOUSE_LOCATION_SUB_STAGE = "1.3";
    private static final String HOUSE_PHOTO_SUB_STAGE = "1.4";
    private static final String LOAN_DETAILS_STAGE = "1.5";
    private static final String REVIEW_SUB_STAGE = "1.6";
//    private static final String SUB_STAGE_STATUS_OPENED = "OPENED";
//    private static final String SUB_STAGE_STATUS_CLOSED = "CLOSED";
//    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String BM_REINTERVIEW_ID_SEQUENCE = "seq_ob_bm_reinterview_id";
    private static final String DECISION_MOVE_TO_CGT = "MOVETOCGT";
    private static final String DECISION_APPROVED = "APPROVED";
    private static final String DECISION_REJECTED = "REJECTED";

    // tb_ob_document classification for a BM-captured house photo -- there's no dedicated
    // category/subCat bucket documented for it, so these follow the closest documented
    // conventions (schema comments on tb_ob_document): legal_doc_name explicitly lists
    // "HOUSE PHOTO" as a valid value; category/sub_cat fall back to the generic "Other" bucket.
    private static final String CATEGORY = "HOUSE_PHOTO";
    private static final String HOUSE_PHOTO_KYC_TYPE = "NONKYC";
    private static final String HOUSE_PHOTO_LEGAL_DOC_NAME = "BM_HOUSE_PHOTO";
    private static final String DOC_STATUS_CAPTURED = "Capture";
    private static final String DOC_STATUS_REUPLOADED = "reuploaded";
    private static final String KYC_DETAILS_WRAPPER_KEY = "kycDetails";

    // Tags this Knowledge Test's entry in tb_ob_customer.learning_session (a list of maps shared
    // with CGT's own day-wise entries -- see OnboardingService) so anyone reading the column can
    // immediately tell which flow produced it, without having to infer it from the entry's shape.
    private static final String KT_LEARNING_SESSION_STAGE = "BMReinterview_Knowledge_Test";

    // AuditService.saveUserAudit's "eventType" -- identifies which API wrote a given
    // tb_ob_user_audit_trail row, same role BM Reinterview's own subStage plays on the
    // tb_ob_bm_reinterview side.
    private static final String AUDIT_EVENT_TYPE = "BM_REINTERVIEW";

    private final TbObBMReInterviewRepository bmReInterviewRepository;
    private final TbObApplicationMasterRepository applicationMasterRepository;
    private final TbObCustomerRepository customerRepository;
    private final TbObLoanRepository loanRepository;
    private final AuditService auditService;
    private final TbObDocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final SequenceUtil sequenceUtil;

    @Value("${onboarding.bm-reinterview.default-kt-question-count:5}")
    private int defaultKtQuestionCount;

    @Value("${onboarding.bm-reinterview.max-kendra-distance}")
    private long maxKendraDistance;

    /**
     * Creates/updates the reinterview record for {@code requestObj.getApplicationId()}, persists
     * any KYC/location details the BM captured (onto {@code tb_ob_customer}), and -- for a real
     * (non-draft) submit carrying a decision -- either advances the application to the decided
     * stage or, for {@code MOVETOCGT}, reverts the whole group back to CGT. Runs as a single
     * transaction: any failure here rolls back every write this call made.
     * <p>
     * Also looks up the {@code tb_ob_loan} row for this application (if any) so the response can
     * carry loan details without the caller needing a separate round trip, and records an audit
     * trail entry for the submission (see {@link #recordAuditTrail}).
     * <p>
     * TODO: notify the Audit/BTS team when {@code custDetails.getGpsMisMatchFlag()} or
     * {@code custDetails.getDistanceFromKendraFlag()} come back non-'N' (BM/KM location mismatch,
     * or house further than {@link #maxKendraDistance} meters from the Kendra) -- not implemented
     * yet.
     */
    @Transactional
    public BMReinterviewOutcome process(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails,
                                        String appId, String userId, String userName, String userRole, String versionNum, String interFace) {

        logger.info("Processing BM ReInterview for Application Id : {}, subStage : {}", requestObj.getApplicationId(), requestObj.getSubStage());

        TbObBMReInterview bmReInterview = saveReInterview(requestObj, custDetails, userId, userName);
        // Only subStage 1.3 (Capture House Location) ever writes to tb_ob_customer. Every other
        // subStage still needs to echo back whatever kyc/location details a previous 1.3 call
        // already captured (so 1.4/1.5/1.6 responses keep showing them, not just 1.3's own
        // response) -- so it's a plain read instead of the fetch+merge write path.
        CustomerDetailsUpdate customerUpdate = HOUSE_LOCATION_SUB_STAGE.equals(requestObj.getSubStage())
                ? updateCustomerRecord(requestObj, custDetails, userId)
                : fetchCustomerDetailsForResponse(requestObj.getCustomerId());

        if (KNOWLEDGE_TEST_SUB_STAGE.equals(requestObj.getSubStage()) && custDetails.getKnowledgeTest() != null) {
            recordKnowledgeTestInLearningSession(requestObj, custDetails, bmReInterview.getKtCompletedTs(), userId);
        }

        if (HOUSE_PHOTO_SUB_STAGE.equals(requestObj.getSubStage()) && custDetails.getHousePhoto() != null) {
            upsertHousePhotoDocument(requestObj, custDetails.getHousePhoto(), userId);
        }

        String decision = custDetails.getDecision();
        boolean revertedToCgt = false;
        if (isRealSubmit(requestObj.getIsDraft()) && isDecisionPresent(decision)) {
            revertedToCgt = updateApplicationStage(decision, requestObj, userId);
        } else {
            logger.debug("No decision to act on for Application Id : {} (isDraft : {}, decision : {})",
                    requestObj.getApplicationId(), requestObj.getIsDraft(), decision);
        }

        TbObLoan loanDetails = loanRepository.findByApplicationId(requestObj.getApplicationId()).orElse(null);

        recordAuditTrail(requestObj, custDetails, bmReInterview, appId, userId, userName, userRole, versionNum, interFace);

        logger.info("BM ReInterview processing completed for Application Id : {}, reinterviewId : {}, revertedToCgt : {}",
                requestObj.getApplicationId(), bmReInterview.getReinterviewId(), revertedToCgt);

        return new BMReinterviewOutcome(bmReInterview, customerUpdate.kycDetails(), customerUpdate.locationDetails(),
                loanDetails, revertedToCgt);
    }

    private TbObBMReInterview saveReInterview(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails,
                                              String userId, String userName) {

        Optional<TbObBMReInterview> existing = bmReInterviewRepository.findByApplicationId(requestObj.getApplicationId());
        logger.info("tb_ob_bm_reinterview lookup for Application Id : {} -- found existing : {}",
                requestObj.getApplicationId(), existing.isPresent());

        TbObBMReInterview bmReInterview = existing.isPresent()
                ? updateExistingReInterview(existing.get(), requestObj, custDetails, userId, userName)
                : createNewReInterview(requestObj, custDetails, userId, userName);

        return bmReInterviewRepository.save(bmReInterview);
    }

    private TbObBMReInterview createNewReInterview(BMReInterviewRequestFields requestObj,
                                                   BMReinterviewCustomerDetails custDetails,
                                                   String bmId, String bmName) {

        logger.info("Creating BM ReInterview.");

        LocalDateTime now = LocalDateTime.now();

        // Fields not owned by any specific sub-stage (per user's choice: questionnaire/decision/
        // rejectionReasons/decisionRemarks are NOT sub-stage-gated -- settable on any call).
        TbObBMReInterview bmReInterview = TbObBMReInterview
                .builder().reinterviewId(sequenceUtil.nextValueAsString(BM_REINTERVIEW_ID_SEQUENCE))
                .applicationId(requestObj.getApplicationId())
                .customerId(requestObj.getCustomerId())
                .bmId(bmId)      // get from userId
                .bmName(bmName)  // get from userName
                .ktQuestionsCount(defaultKtQuestionCount)
                .docVerifyPayload("")
                .ktAnswers("")
                .questionnaireAnswers(toJsonOrEmpty(buildQuestionnaire(custDetails)))
                .decision(custDetails.getDecision())
                .rejectionReasons(toJsonOrEmpty(custDetails.getRejectionReasons()))
                .decisionRemarks(custDetails.getDecisionRemarks())
                .decisionTs(isDecisionPresent(custDetails.getDecision()) ? now : null)
                .status(deriveStatus(requestObj, null))
//                .status(deriveStatus(requestObj.getSubStageStatus(), null))
                .subStage(buildSubStagePayload(null, requestObj.getSubStage(), !isRealSubmit(requestObj.getIsDraft())))
                .subStageStatus(requestObj.getSubStageStatus())
                .reinterviewStartTs(now)
                .reinterviewEndTs(isReInterviewEnding(requestObj, custDetails.getDecision()) ? now : null)
                .createdTs(now)
                .build();

        applyStageSpecificFields(bmReInterview, requestObj, custDetails);

        return bmReInterview;
    }

    private TbObBMReInterview updateExistingReInterview(TbObBMReInterview bmReInterview, BMReInterviewRequestFields requestObj,
                                                        BMReinterviewCustomerDetails custDetails, String bmId, String bmName) {

        logger.info("Updating Existing BM ReInterview.");

        LocalDateTime now = LocalDateTime.now();

        bmReInterview.setBmId(bmId); // get from userId
        bmReInterview.setBmName(bmName); // get from userName

        applyStageSpecificFields(bmReInterview, requestObj, custDetails);

        // Not sub-stage-gated (per user's choice) -- settable on any call, guarded only on the
        // incoming custDetails actually carrying that field's data, since the caller only sends
        // whatever's relevant to the subStage screen it's currently submitting.
        if (custDetails.getQuestionnaire() != null && custDetails.getQuestionnaire().getAnswers() != null
                && !custDetails.getQuestionnaire().getAnswers().isEmpty()) {
            bmReInterview.setQuestionnaireAnswers(toJsonOrEmpty(buildQuestionnaire(custDetails)));
        }
        if (isDecisionPresent(custDetails.getDecision())) {
            bmReInterview.setDecision(custDetails.getDecision());
        }
        if (custDetails.getRejectionReasons() != null && !custDetails.getRejectionReasons().isEmpty()) {
            bmReInterview.setRejectionReasons(toJsonOrEmpty(custDetails.getRejectionReasons()));
        }
        if (custDetails.getDecisionRemarks() != null) {
            bmReInterview.setDecisionRemarks(custDetails.getDecisionRemarks());
        }

        if (isDecisionPresent(custDetails.getDecision()) && bmReInterview.getDecisionTs() == null) {
            bmReInterview.setDecisionTs(now);
        }

//        bmReInterview.setStatus(deriveStatus(requestObj.getSubStageStatus(), bmReInterview.getStatus()));
        bmReInterview.setStatus(deriveStatus(requestObj, bmReInterview.getStatus()));
        bmReInterview.setSubStage(buildSubStagePayload(bmReInterview.getSubStage(), requestObj.getSubStage(), !isRealSubmit(requestObj.getIsDraft())));
        bmReInterview.setSubStageStatus(requestObj.getSubStageStatus());

        if (bmReInterview.getReinterviewEndTs() == null
                && isReInterviewEnding(requestObj, custDetails.getDecision())) {
            bmReInterview.setReinterviewEndTs(now);
        }

        bmReInterview.setUpdatedBy(bmId);
        bmReInterview.setUpdatedTs(now);

        logger.info("Existing BM ReInterview updated successfully.");
        return bmReInterview;
    }

    /**
     * Applies whichever field group belongs to {@code requestObj.getSubStage()} -- and only that
     * group -- shared by both {@link #createNewReInterview} and {@link #updateExistingReInterview}
     * so the mapping lives in exactly one place:
     * <pre>
     *   1.1 Document Verification -> docVerified / docVerifiedTs / docVerifyPayload
     *   1.2 Knowledge Test        -> ktQuestionsCount / ktScore / ktAnswers / ktCompletedTs
     *                                 (also recorded as its own entry in tb_ob_customer.learning_session -- see recordKnowledgeTestInLearningSession)
     *   1.3 Capture House Location -> gpsDistanceBmKm, gpsMismatchFlag, distFromKendraM, distFromKendraFlag, locationCapturedTs
     *                                 (the BM/KM GPS points themselves go to tb_ob_customer.location_details -- see updateCustomerRecord)
     *   1.4 Capture Member House Photo -> housePhotoDocId/housePhotoClarity/housePhotoClarityPass/housePhotoTs
     *                                 (also inserted as its own tb_ob_document row -- see upsertHousePhotoDocument)
     *   1.5 Loan Details           -> loanEditedByBm
     * </pre>
     * A call for one sub-stage never touches another sub-stage's fields, even if the payload
     * happens to carry that data (e.g. a stray {@code housePhoto} object sent alongside
     * subStage=1.1 is ignored) -- each field group is only ever written by its own sub-stage's
     * call. 1.6 (Review) has no field group of its own here; questionnaire/decision/
     * rejectionReasons/decisionRemarks are handled separately in the callers since they're not
     * sub-stage-gated.
     * <p>
     * Each sub-stage's own timestamp ({@code docVerifiedTs}/{@code ktCompletedTs}/
     * {@code locationCapturedTs}/{@code housePhotoTs}) follows the same rule: only stamped the
     * first time that sub-stage is submitted as a real submit (isDraft=false/absent) -- once
     * set, no later call (draft or real, same sub-stage or not) ever overwrites it.
     */
    private void applyStageSpecificFields(TbObBMReInterview bmReInterview, BMReInterviewRequestFields requestObj,
                                          BMReinterviewCustomerDetails custDetails) {

        String subStage = requestObj.getSubStage();
        boolean isRealSubmit = isRealSubmit(requestObj.getIsDraft());
        logger.info("Applying stage-specific fields for subStage : {} (isRealSubmit : {})", subStage, isRealSubmit);

        if (DOC_VERIFICATION_SUB_STAGE.equals(subStage)) {

            if (custDetails.getDocVerified() != null) {
                bmReInterview.setDocVerified(custDetails.getDocVerified());
            }
            if (isRealSubmit && bmReInterview.getDocVerifiedTs() == null) {
                bmReInterview.setDocVerifiedTs(getTimeStamp());
            }
            if (custDetails.getDocumentVerification() != null && !custDetails.getDocumentVerification().isEmpty()) {
                bmReInterview.setDocVerifyPayload(toJsonOrEmpty(buildDocumentPayload(custDetails)));
            }

        } else if (KNOWLEDGE_TEST_SUB_STAGE.equals(subStage)) {

            if (custDetails.getKnowledgeTest() != null) {
                bmReInterview.setKtQuestionsCount(custDetails.getKnowledgeTest().getTotalQuestions());
                bmReInterview.setKtScore(custDetails.getKnowledgeTest().getScore());
                bmReInterview.setKtAnswers(toJsonOrEmpty(buildKtAnswers(custDetails)));

                if (isRealSubmit && bmReInterview.getKtCompletedTs() == null) {
                    bmReInterview.setKtCompletedTs(getTimeStamp());
                }
            }

        } else if (HOUSE_LOCATION_SUB_STAGE.equals(subStage)) {

            // custDetails.getLocationDetails() (the raw BM/KM GPS points) is persisted separately
            // onto tb_ob_customer.location_details -- see updateCustomerRecord() -- this entity no
            // longer has dedicated bm_gps_latitude/longitude / km_gps_latitude/longitude columns.
            if (custDetails.getGpsDistanceBmKm() != null) {
                bmReInterview.setGpsDistanceBmKm(custDetails.getGpsDistanceBmKm());
            }
            if (custDetails.getGpsMisMatchFlag() != null) {
                bmReInterview.setGpsMismatchFlag(custDetails.getGpsMisMatchFlag());
            }
            if (custDetails.getDistanceFromKendra() != null) {
                bmReInterview.setDistFromKendraM(custDetails.getDistanceFromKendra());
            }
            if (custDetails.getDistanceFromKendraFlag() != null) {
                bmReInterview.setDistFromKendraFlag(custDetails.getDistanceFromKendraFlag());
            }
            if (isRealSubmit && bmReInterview.getLocationCapturedTs() == null) {
                bmReInterview.setLocationCapturedTs(getTimeStamp());
            }

        } else if (HOUSE_PHOTO_SUB_STAGE.equals(subStage)) {

            if (custDetails.getHousePhoto() != null) {
                bmReInterview.setHousePhotoDocId(custDetails.getHousePhoto().getHousePhotoDocId());
                bmReInterview.setHousePhotoClarity(custDetails.getHousePhoto().getClarityScore());
                bmReInterview.setHousePhotoClarityPass(custDetails.getHousePhoto().getHousePhotoClarityPass());

                if (isRealSubmit && bmReInterview.getHousePhotoTs() == null) {
                    bmReInterview.setHousePhotoTs(getTimeStamp());
                }
            }

        } else if (LOAN_DETAILS_STAGE.equals(subStage)) {

            if (custDetails.getLoanEditedByBm() != null) {
                bmReInterview.setLoanEditedByBm(custDetails.getLoanEditedByBm());
            }

        } else if (REVIEW_SUB_STAGE.equals(subStage)) {
            // 1.6 Review has no field group of its own -- the subStage entry itself
            // (e.g. {"subStage":"1.6","verified":"N"/"Y"}) is recorded generically by
            // buildSubStagePayload() right after this method returns, same as every other
            // sub-stage, so no additional field writes are needed here.
        }
        else {
            logger.warn("Unrecognized subStage : {} -- no field group applied, only the generic subStage entry is recorded.", subStage);
        }
    }

    /**
     * Updates whichever of {@code tb_ob_customer.location_details} / {@code .kyc_details} this
     * call actually carries data for, in a single fetch+save, and either way returns the
     * customer's current values for both so {@link BMReinterviewResponseMapper} can echo them
     * back. Only ever called for subStage 1.3 (Capture House Location) -- see the call site in
     * {@link #process} -- since that's the only screen the BM app actually captures GPS/KYC on
     * during a house visit; every other subStage skips this method entirely rather than paying
     * for a customer fetch whose result would just be thrown away.
     * <p>
     * {@code locationDetails} (the BM/KM GPS points) is stored as a straight overwrite -- it's a
     * point-in-time capture, not something to accumulate -- while {@code kycDetails} is merged
     * into whatever's already stored. Both columns are plain TEXT, so the Java-side
     * representation is serialized to/parsed from a JSON string via
     * {@link #toJsonOrEmpty}/{@link #fromJsonOrEmptyMap} rather than a native jsonb map.
     */
    private CustomerDetailsUpdate updateCustomerRecord(BMReInterviewRequestFields requestObj,
                                                       BMReinterviewCustomerDetails custDetails, String userId) {

        TbObCustomer customer = customerRepository.findByCustomerId(requestObj.getCustomerId())
                .orElseThrow(() -> new BMReinterviewValidationException("Customer Id not found in tb_ob_customer table: " + requestObj.getCustomerId()));

        boolean modified = false;

        if (custDetails.getLocationDetails() != null && !custDetails.getLocationDetails().isEmpty()) {
            logger.info("locationDetails present on this call ({} entries) -- overwriting tb_ob_customer.location_details for Customer Id : {}",
                    custDetails.getLocationDetails().size(), requestObj.getCustomerId());
            customer.setLocationDetails(toJsonOrEmpty(custDetails.getLocationDetails()));
            modified = true;
        }

//        Map<String, Object> kycDetails = fromJsonOrEmptyMap(customer.getKycDetails()); // TODO
        Map<String, Object> kycDetails = customer.getKycDetails();

        if (custDetails.getKycDetails() != null) {
            logger.info("kycDetails present on this call -- merging into tb_ob_customer.kyc_details for Customer Id : {}",
                    requestObj.getCustomerId());

            BMReinterviewLocationDetails bmGpsInfo = resolveBmGpsInfo(custDetails, customer);

            BMReinterviewKycDetails kyc = custDetails.getKycDetails();
            kyc.setGpsLat(bmGpsInfo == null ? null : bmGpsInfo.getLat());
            kyc.setGetLong(bmGpsInfo == null ? null : bmGpsInfo.getLon());

            Map<String, Object> incomingKyc = objectMapper.convertValue(kyc, new TypeReference<Map<String, Object>>() {});
            incomingKyc.values().removeIf(Objects::isNull);
            kycDetails.putAll(incomingKyc);

//            customer.setKycDetails(toJsonOrEmpty(kycDetails));
            customer.setKycDetails(kycDetails);
            modified = true;
        }

        if (modified) {
            customer.setUpdatedBy(userId);
            customer.setUpdatedTs(LocalDateTime.now());
            customerRepository.save(customer);
            logger.info("tb_ob_customer location/KYC details updated for Customer Id : {}", requestObj.getCustomerId());
        }
        else {
            logger.debug("Neither locationDetails nor kycDetails present on this call -- tb_ob_customer left untouched for Customer Id : {}",
                    requestObj.getCustomerId());
        }

        return new CustomerDetailsUpdate(kycDetails, customer.getLocationDetails());
    }

    /** {@code kycDetails} merged and ready for the response; {@code locationDetails} as its raw stored JSON text. */
    private record CustomerDetailsUpdate(Map<String, Object> kycDetails, String locationDetails) {

        /** Used when {@code customerId} doesn't (yet) resolve to a {@code tb_ob_customer} row at all. */
        static CustomerDetailsUpdate empty() {
            return new CustomerDetailsUpdate(null, null);
        }
    }

    /**
     * Read-only counterpart to {@link #updateCustomerRecord}, used for every subStage other than
     * 1.3 (Capture House Location) so the response still carries whatever kyc/location details an
     * earlier 1.3 call already captured -- e.g. subStage 1.6's response should show 1.1 through 1.6
     * details, not just 1.6's own. Never writes to {@code tb_ob_customer}. Returns
     * {@link CustomerDetailsUpdate#empty()} if the customer hasn't been captured at all yet (e.g.
     * this application never reached subStage 1.3), same as before this method existed.
     */
    private CustomerDetailsUpdate fetchCustomerDetailsForResponse(String customerId) {

        return customerRepository.findByCustomerId(customerId)
                .map(customer -> new CustomerDetailsUpdate(
                        customer.getKycDetails(), customer.getLocationDetails()))
//                        extractKycDetailsMap(customer.getKycDetails()), customer.getLocationDetails())) // TODO if kyc is TEXT
                .orElseGet(CustomerDetailsUpdate::empty);
    }

    /**
     * {@code tb_ob_customer.kyc_details} stores its JSON one level deeper than you'd expect --
     * the column's own root object has a single {@code "kycDetails"} key holding the actual
     * fields (depName, PA, CA, dob, name, primaryType, ..., gpsLat, gpsLong), rather than those
     * fields sitting at the root themselves. This unwraps that root and returns the inner map (an
     * empty, mutable one if the column is null/blank, or if the {@code "kycDetails"} key is
     * missing/not an object). Everywhere else in this class (the outcome returned to the response
     * mapper, the merge logic above) works with this inner map directly -- only the physical
     * storage is wrapped, not the in-memory/response representation.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractKycDetailsMap(String storedJson) {

        Map<String, Object> stored = fromJsonOrEmptyMap(storedJson);
        Object inner = stored.get(KYC_DETAILS_WRAPPER_KEY);
        return inner instanceof Map ? new LinkedHashMap<>((Map<String, Object>) inner) : new LinkedHashMap<>();
    }

    /** Re-wraps the merged KYC map under {@code "kycDetails"} for storage -- see {@link #extractKycDetailsMap}. */
    private String wrapKycDetailsMap(Map<String, Object> kycDetails) {
        return toJsonOrEmpty(Collections.singletonMap(KYC_DETAILS_WRAPPER_KEY, kycDetails));
    }

    /**
     * Records the house photo captured at subStage 1.4 as its own {@code tb_ob_document} row --
     * separate from the {@code house_photo_doc_id}/{@code clarity}/{@code clarityPass}/{@code ts}
     * columns {@link #applyStageSpecificFields} already writes onto {@code tb_ob_bm_reinterview}
     * itself -- since {@code tb_ob_document} is the shared document store every other
     * document-capture flow in onboarding (KYC, family, address, GRT house visit) feeds into.
     * <p>
     * A house-photo row already existing for this application is treated as a re-upload (new
     * photo, {@code docVersion} bumped, {@code isEdited}/{@code reuploadedBy} recorded) rather
     * than inserting a duplicate row on every resubmission of the same subStage.
     */
    private void upsertHousePhotoDocument(BMReInterviewRequestFields requestObj, HousePhotoRequestFields housePhoto, String userId) {

        LocalDateTime uploadedAt = housePhoto.getHousePhotoTs() != null
                ? Instant.ofEpochMilli(housePhoto.getHousePhotoTs()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        Optional<TbObDocument> existing = documentRepository.findByApplicationIdAndLegalDocName(
                requestObj.getApplicationId(), HOUSE_PHOTO_LEGAL_DOC_NAME);

        TbObDocument document;
        if (existing.isPresent()) {
            document = existing.get();
            document.setPhoto(housePhoto.getHousePhotoDocId());
            document.setClarityScore(housePhoto.getClarityScore());
            document.setClarityPass(housePhoto.getHousePhotoClarityPass());
            document.setStatus(DOC_STATUS_REUPLOADED);
            document.setIsEdited(true);
            document.setReuploadedBy(userId);
            document.setDocVersion((document.getDocVersion() == null ? 1 : document.getDocVersion()) + 1);
            document.setUploadedBy(userId);
            document.setUploadedAt(uploadedAt);
            document.setUpdatedTs(LocalDateTime.now());
            logger.info("tb_ob_document house photo re-uploaded -- Application Id : {}, docuId : {}, docVersion : {}",
                    document.getApplicationId(), document.getDocuId(), document.getDocVersion());
        } else {
            document = TbObDocument.builder()
                    .applicationId(requestObj.getApplicationId())
                    .docuId(nextDocuId(requestObj.getApplicationId()))
                    .customerId(requestObj.getCustomerId())
                    .category(CATEGORY)
                    .subCat(CATEGORY)
                    .kycType(HOUSE_PHOTO_KYC_TYPE)
                    .legalDocName(HOUSE_PHOTO_LEGAL_DOC_NAME)
                    .photo(housePhoto.getHousePhotoDocId())
                    .status(DOC_STATUS_CAPTURED)
                    .isEdited(false)
                    .clarityScore(housePhoto.getClarityScore())
                    .clarityPass(housePhoto.getHousePhotoClarityPass())
//                    .dedupeStatus(NOT_APPLICABLE)
//                    .validationStatus(NOT_APPLICABLE)
                    .docVersion(1)
                    .uploadedBy(userId)
                    .uploadedAt(uploadedAt)
                    .createdTs(LocalDateTime.now())
                    .build();
            logger.info("tb_ob_document house photo captured -- Application Id : {}, docuId : {}",
                    document.getApplicationId(), document.getDocuId());
        }

        documentRepository.save(document);
    }

    /**
     * {@code docu_id} resets per application (1, 2, 3, ...) rather than being a global sequence,
     * so the next one is computed from whatever's already on file for this application.
     */
    private String nextDocuId(String applicationId) {
        int maxExisting = documentRepository.findByApplicationId(applicationId).stream()
                .mapToInt(document -> parseDocuIdOrZero(document.getDocuId()))
                .max()
                .orElse(0);
        return String.valueOf(maxExisting + 1);
    }

    private int parseDocuIdOrZero(String docuId) {
        try {
            return Integer.parseInt(docuId);
        } catch (NumberFormatException ex) {
            logger.warn("Non-numeric docu_id encountered : {} -- treating as 0 for next-id computation.", docuId);
            return 0;
        }
    }

    /**
     * Records this Knowledge Test submission as its own entry in
     * {@code tb_ob_customer.learning_session} -- the same list-of-maps column the CGT flow (see
     * OnboardingService) writes its day-wise entries into, so a single place shows a member's whole
     * learning history across both flows. {@code "stage"} is deliberately inserted as the first key
     * (via {@link LinkedHashMap}'s insertion order) so anyone reading the raw JSON immediately sees
     * which flow this entry came from, rather than having to infer it from the entry's shape.
     * Resubmitting subStage 1.2 updates that one entry in place instead of duplicating it, matched
     * by its {@code "stage"} key -- the same merge discipline CGT already applies to its own
     * day-wise entries.
     */
    private void recordKnowledgeTestInLearningSession(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails,
                                                      LocalDateTime completedTs, String userId) {

        TbObCustomer customer = customerRepository.findByCustomerId(requestObj.getCustomerId())
                .orElseThrow(() -> new BMReinterviewValidationException("Customer Id not found in tb_ob_customer table: " + requestObj.getCustomerId()));

        List<Map<String, Object>> learningSession = fromJsonOrEmptyList(customer.getLearningSession());

        Map<String, Object> ktEntry = new LinkedHashMap<>();
        ktEntry.put("stage", KT_LEARNING_SESSION_STAGE);
        ktEntry.put("totalQuestions", custDetails.getKnowledgeTest().getTotalQuestions());
        ktEntry.put("score", custDetails.getKnowledgeTest().getScore());
        ktEntry.put("answers", buildKtAnswers(custDetails));
        // Epoch millis, not a raw LocalDateTime -- same convention DocumentVerificationRequest's
        // own verifiedTs already uses for a timestamp embedded in one of these JSON-text payloads,
        // and avoids this column's serialization depending on the JSON mapper having a
        // java.time module registered.
        ktEntry.put("completedTs", completedTs == null ? null : completedTs.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        boolean updated = false;
        for (int i = 0; i < learningSession.size(); i++) {
            if (KT_LEARNING_SESSION_STAGE.equals(learningSession.get(i).get("stage"))) {
                learningSession.set(i, ktEntry);
                updated = true;
                break;
            }
        }
        if (!updated) {
            learningSession.add(ktEntry);
        }

        customer.setLearningSession(toJsonOrEmpty(learningSession));
        customer.setUpdatedBy(userId);
        customer.setUpdatedTs(LocalDateTime.now());
        customerRepository.save(customer);

        logger.info("tb_ob_customer.learning_session updated with BM Reinterview Knowledge Test entry ({}) for Customer Id : {}",
                updated ? "in place" : "newly added", requestObj.getCustomerId());
    }

    /**
     * The BM's GPS point to stamp onto {@code kycDetails.gpsLat}/{@code gpsLong} -- preferring
     * whatever this call's request actually sent, but never depending on it: if this particular
     * call didn't (re)send {@code locationDetails} (e.g. the BM only touched KYC fields this time),
     * falling back to whatever's already stored on {@code tb_ob_customer.location_details} keeps
     * the KYC record's GPS point in sync with the customer's last known location instead of going
     * stale/null. {@code customer} is read here rather than the request because, by the time this
     * runs, it already reflects this call's own {@code locationDetails} write (if any) -- so the
     * fallback is always "the current location on file", whether that came from this call or an
     * earlier one.
     */
    private BMReinterviewLocationDetails resolveBmGpsInfo(BMReinterviewCustomerDetails custDetails, TbObCustomer customer) {

        BMReinterviewLocationDetails bmGpsInfo = findLocationByRole(custDetails.getLocationDetails(), BM);
        if (bmGpsInfo != null) {
            return bmGpsInfo;
        }

        logger.debug("No BM locationDetails on this call -- falling back to tb_ob_customer.location_details on file for Customer Id : {}",
                customer.getCustomerId());
        return findLocationByRole(fromJsonOrEmptyLocationList(customer.getLocationDetails()), BM);
    }

    /**
     * Finds the location entry for a given role (KM/BM) by its own {@code userRole} field.
     */
    private BMReinterviewLocationDetails findLocationByRole(List<BMReinterviewLocationDetails> locationDetails, String role) {

        if (locationDetails == null) {
            return null;
        }
        return locationDetails.stream()
                .filter(entry -> role.equalsIgnoreCase(entry.getUserRole()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Deserializes {@code tb_ob_customer.location_details} (TEXT column storing a JSON array) back
     * into {@code List<BMReinterviewLocationDetails>}. A null/blank value is treated as an empty
     * list rather than an error, same as the other {@code fromJsonOrEmpty*} helpers.
     */
    private List<BMReinterviewLocationDetails> fromJsonOrEmptyLocationList(String json) {

        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<BMReinterviewLocationDetails>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing stored JSON value.", e);
        }
    }

    /**
     * subStage tracks which BM Reinterview screen is active (1.1 Document Verification ... 1.6
     * Review) and subStageStatus whether it's OPENED (in progress) or CLOSED (completed). The
     * record's overall status mirrors whichever subStage was last touched, regardless of decision.
     */
//    private String deriveStatus(String subStageStatus, String existingStatus) {
//
//        if (SUB_STAGE_STATUS_OPENED.equalsIgnoreCase(subStageStatus)) {
//            return STATUS_IN_PROGRESS;
//        }
//        if (SUB_STAGE_STATUS_CLOSED.equalsIgnoreCase(subStageStatus)) {
//            return STATUS_COMPLETED;
//        }
//        return existingStatus != null ? existingStatus : STATUS_PENDING;
//    }

    /**
     * The record starts {@code IN_PROGRESS} the first time it's ever created and stays that way
     * through every sub-stage -- it only ever moves to {@code COMPLETED} once, on a real (non-draft)
     * submit of subStage 1.6 (Review), i.e. exactly when {@link #isReviewCompleted} says so.
     */
    private String deriveStatus(BMReInterviewRequestFields requestObj, String existingStatus) {

        if (isReviewCompleted(requestObj)) {
            return STATUS_COMPLETED;
        }
        return existingStatus != null ? existingStatus : STATUS_IN_PROGRESS;
    }

    /** True on a real (non-draft) submit of subStage 1.6 (Review) -- the only trigger for {@code COMPLETED}. */
    private boolean isReviewCompleted(BMReInterviewRequestFields requestObj) {
        return REVIEW_SUB_STAGE.equals(requestObj.getSubStage()) && isRealSubmit(requestObj.getIsDraft());
    }

    /**
     * Merges this submission's subStage into the accumulated {@code sub_stage} JSON array on
     * {@code tb_ob_bm_reinterview} -- one entry per subStage encountered so far, keyed by
     * {@code subStage} value ("1.1", "1.2", ...) so re-submitting the same subStage updates its
     * entry in place instead of duplicating it (same pattern as CGT's day-wise merge in
     * OnboardingService). {@code verified} is "Y" for a real submit (isDraft=false/absent)
     * touching that subStage, "N" for a draft save (isDraft=true) -- regardless of what
     * subStageStatus (OPENED/CLOSED) was sent. A null/blank incoming subStage leaves the
     * accumulated array untouched.
     */
    private String buildSubStagePayload(String existingSubStageJson, String subStage, boolean isDraft) {

        if (subStage == null || subStage.isBlank()) {
            return existingSubStageJson;
        }

        List<Map<String, Object>> subStages = fromJsonOrEmptyList(existingSubStageJson);
        String verified = isDraft ? "N" : "Y";

        boolean updated = false;
        for (Map<String, Object> entry : subStages) {
            if (subStage.equals(entry.get("subStage"))) {
                entry.put("verified", verified);
                updated = true;
                break;
            }
        }
        if (!updated) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("subStage", subStage);
            entry.put("verified", verified);
            subStages.add(entry);
        }
        logger.debug("subStage {} entry {} in accumulated sub_stage JSON (verified : {})",
                subStage, updated ? "updated in place" : "added", verified);

        return toJsonOrEmpty(subStages);
    }

    /**
     * Deserializes {@code tb_ob_bm_reinterview.sub_stage} (TEXT column storing a JSON array) back
     * into a mutable {@code List<Map<String, Object>>} so {@link #buildSubStagePayload} can
     * update it in place. A null/blank value (first subStage ever recorded) is treated as an
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

//    private boolean isReInterviewEnding(BMReInterviewRequestFields requestObj, String decision) {
//
//        boolean lastSubStageClosed = LOAN_DETAILS_STAGE.equals(requestObj.getSubStage())
//                && SUB_STAGE_STATUS_CLOSED.equalsIgnoreCase(requestObj.getSubStageStatus());
//        return lastSubStageClosed || isDecisionPresent(decision);
//    }

    private boolean isReInterviewEnding(BMReInterviewRequestFields requestObj, String decision) {
        return isReviewCompleted(requestObj) || isDecisionPresent(decision);
    }

    private boolean isDecisionPresent(String decision) {
        return decision != null && !decision.isBlank();
    }

    /**
     * A real submit is anything that wasn't explicitly flagged as a draft -- {@code isDraft} being
     * absent (null) counts as real, same as {@code isDraft=false}. Used consistently everywhere
     * "did the BM actually submit this sub-stage" needs deciding (sub-stage timestamps, whether to
     * act on a decision), so a caller that omits {@code isDraft} on a genuine submit is never
     * mistaken for a draft save.
     */
    private boolean isRealSubmit(Boolean isDraft) {
        return !Boolean.TRUE.equals(isDraft);
    }

    private boolean updateApplicationStage(String decision, BMReInterviewRequestFields requestObj, String userId) {

        logger.info("Updating tb_ob_application_master for Application Id : {} with decision : {}",
                requestObj.getApplicationId(), decision);

        if (DECISION_MOVE_TO_CGT.equalsIgnoreCase(decision)) {
            revertGroupToCGTStage(requestObj, userId);
            return true;
        }

        TbObApplicationMaster application = applicationMasterRepository.findByApplicationId(requestObj.getApplicationId())
                .orElseThrow(() -> new BMReinterviewValidationException("Application not found."));

        switch (decision.toUpperCase()) {
            case DECISION_APPROVED:
                application.setStage("GRT");
                break;
            case DECISION_REJECTED:
                application.setStage("BMREJECTED");
                break;
            default:
                logger.error("Invalid decision taken by the user : {}. choose either APPROVED/MOVETOCGT/REJECTED", decision.toUpperCase());
                throw new BMReinterviewValidationException("Invalid decision taken by the user. choose either APPROVED/MOVETOCGT/REJECTED");
        }

        application.setVersion(nextVersion(application.getVersion()));
        application.setUpdatedBy(userId);
        application.setUpdatedTs(LocalDateTime.now());
        applicationMasterRepository.save(application);
        logger.info("tb_ob_application_master updated -- Application Id : {}, new stage : {}, version : {}",
                requestObj.getApplicationId(), application.getStage(), application.getVersion());
        return false;
    }

    private void revertGroupToCGTStage(BMReInterviewRequestFields requestObj, String userId) {

        logger.info("Reverting Group Id : {} back to CGT stage.", requestObj.getGroupId());

        List<TbObApplicationMaster> applications = applicationMasterRepository
                .findByGroupIdAndStatusNot(requestObj.getGroupId(), ApplicationStatus.REJECTED.name());

        if (applications.isEmpty()) {
            logger.error("No application id found in the tb_ob_application table while search by groupId. group id : {}", requestObj.getGroupId());
            throw new BMReinterviewValidationException("No application found for the given Group Id.");
        }

        LocalDateTime now = LocalDateTime.now();
        String revertRemark = "Candidate and assigned group reverted back to CGT stage from BM stage.";

        for (TbObApplicationMaster application : applications) {
            application.setStage("CGT");
            application.setVersion(nextVersion(application.getVersion()));
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


    /**
     * Writes an application-level audit trail row ({@code tb_ob_cust_audit_trail}, via
     * {@link AuditService#saveApplicationAudit}) and a user-level one ({@code tb_ob_user_audit_trail},
     * via {@link AuditService#saveUserAudit}) for this submission -- one pair per call to
     * {@link #process}, regardless of subStage, mirroring how other services in this codebase
     * (e.g. the application-update flow this pattern is based on) audit every update rather than
     * only decision-driven ones.
     * <p>
     * Both {@code applicationMasterRepository}/{@code customerRepository} lookups here are
     * independent, read-only, best-effort fetches purely for audit metadata (kendraId/groupId/
     * branchId/customerName/mobile number) -- deliberately separate from the mandatory,
     * throwing lookups {@link #updateApplicationStage} and {@link #updateCustomerRecord} already
     * do for their own (write) purposes elsewhere in {@link #process}. If either record can't be
     * found, the audit entries are skipped rather than failing the whole submission: both
     * {@link AuditService} methods already swallow and log their own exceptions internally, so a
     * write failure here was never going to roll back the transaction anyway -- auditing is
     * strictly a side effect of a successful submission, not a precondition for one.
     */
    private void recordAuditTrail(BMReInterviewRequestFields requestObj, BMReinterviewCustomerDetails custDetails,
                                  TbObBMReInterview bmReInterview, String appId, String userId, String userName, String userRole, String versionNum, String interFace) {

        TbObApplicationMaster master = applicationMasterRepository.findByApplicationId(requestObj.getApplicationId()).orElse(null);
        if (master == null) {
            logger.warn("Skipping audit trail -- Application Id : {} not found in tb_ob_application_master.", requestObj.getApplicationId());
            return;
        }

        TbObCustomer customer = customerRepository.findByCustomerId(requestObj.getCustomerId()).orElse(null);
        if (customer == null) {
            logger.warn("Skipping audit trail -- Customer Id : {} not found in tb_ob_customer.", requestObj.getCustomerId());
            return;
        }

        auditService.saveApplicationAudit(master, customer, userId, userName, userRole, appId, null,
                master.getStage(), master.getSubStage(), master.getWfStage(), custDetails, buildModifiedDetails(custDetails), null);
        //versionNum TODO

        auditService.saveUserAudit(interFace, requestObj.getApplicationId(), requestObj.getCustomerId(),
                userId, userName, userRole, master.getBranchId(), master.getKendraId(), master.getGroupId(), custDetails);
    }

    /**
     * The subset of this call's {@code custDetails} that actually carries data -- i.e. which
     * fields the BM sent on this particular subStage submission -- for
     * {@link AuditService#saveApplicationAudit}'s {@code modifiedDetails} parameter. Distinct from
     * that same call's {@code payload} argument (also {@code custDetails}, passed as-is): payload
     * is the raw request exactly as sent, while modifiedDetails is what {@link AuditService}
     * flattens into dotted field paths (see its {@code flattenModifiedDetails}) for the audit
     * trail's {@code editedDetails}/{@code isEdited} columns, so untouched (null) fields are
     * stripped here first -- same discipline the KYC merge in {@link #updateCustomerRecord}
     * already applies for the same reason. Returns {@code null} (not an empty map) when nothing
     * was actually sent, so {@code AuditService} correctly records {@code isEdited = false}.
     */
    private Map<String, Object> buildModifiedDetails(BMReinterviewCustomerDetails custDetails) {

        Map<String, Object> modifiedDetails = objectMapper.convertValue(custDetails, new TypeReference<Map<String, Object>>() {});
        modifiedDetails.values().removeIf(Objects::isNull);
        return modifiedDetails.isEmpty() ? null : modifiedDetails;
    }

    /** Parses a version string, defaulting a null/absent value to "0", and returns the next one. */
    private String nextVersion(String version) {
        return String.valueOf(Integer.parseInt(version == null ? "0" : version) + 1);
    }

    private LocalDateTime getTimeStamp() {
        return LocalDateTime.now();
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
     * Deserializes {@code tb_ob_application_master.add_info1} (a TEXT column) back into a mutable
     * {@code Map<String, Object>} so callers can add/overwrite a key and persist it again via
     * {@link #toJsonOrEmpty}. A null/blank value is treated as an empty, mutable map.
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
}
