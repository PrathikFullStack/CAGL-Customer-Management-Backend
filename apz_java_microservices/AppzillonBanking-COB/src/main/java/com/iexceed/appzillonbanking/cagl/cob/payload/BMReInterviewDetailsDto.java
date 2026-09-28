package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full snapshot of {@code TbObBMReInterview} for a single application. JSON-text
 * columns (docVerifyPayload, ktAnswers, questionnaireAnswers, rejectionReasons) are
 * carried through as raw strings — {@code JsonInliningUtil} inlines them into nested
 * JSON when the response is serialized, same as every other stored-JSON column.
 */
@Builder
public record BMReInterviewDetailsDto(
        String reinterviewId,
        String applicationId,
        String customerId,
        String bmId,
        String bmName,
        Boolean docVerified,
        LocalDateTime docVerifiedTs,
        String docVerifyPayload,
        Integer ktQuestionsCount,
        Integer ktScore,
        String ktAnswers,
        LocalDateTime ktCompletedTs,
        BigDecimal bmGpsLatitude,
        BigDecimal bmGpsLongitude,
        BigDecimal bmGpsAccuracy,
        BigDecimal kmGpsLatitude,
        BigDecimal kmGpsLongitude,
        BigDecimal gpsDistanceBmKm,
        Character gpsMismatchFlag,
        BigDecimal distFromKendraM,
        Character distFromKendraFlag,
        LocalDateTime locationCapturedTs,
        String housePhotoDocId,
        BigDecimal housePhotoClarity,
        Character housePhotoClarityPass,
        LocalDateTime housePhotoTs,
        Character loanEditedByBm,
        String questionnaireAnswers,
        String decision,
        String rejectionReasons,
        String decisionRemarks,
        LocalDateTime decisionTs,
        String status,
        String subStage,
        String subStageStatus,
        LocalDateTime reinterviewStartTs,
        LocalDateTime reinterviewEndTs,
        LocalDateTime createdTs,
        LocalDateTime updatedTs,
        String updatedBy
) {
}