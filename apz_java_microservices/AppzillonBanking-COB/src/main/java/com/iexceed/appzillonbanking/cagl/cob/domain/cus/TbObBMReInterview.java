package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "tb_ob_bm_reinterview")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObBMReInterview {

    @Id
    @Column(name = "reinterview_id", nullable = false)
    @JsonProperty("reinterviewId")
    private String reinterviewId;

    @Column(name = "application_id", nullable = false)
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "customer_id", nullable = false)
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "bm_id")
    @JsonProperty("bmId")
    private String bmId;

    @Column(name = "bm_name")
    @JsonProperty("bmName")
    private String bmName;

    @Column(name = "doc_verified")
    @JsonProperty("docVerified")
    @Builder.Default
    private Boolean docVerified = false;

    @Column(name = "doc_verified_ts")
    @JsonProperty("docVerifiedTs")
    private LocalDateTime docVerifiedTs;

    @JsonProperty("docVerifyPayload")
    @Column(name = "doc_verify_payload", columnDefinition = "TEXT")
    private String docVerifyPayload;

    @Column(name = "kt_questions_count")
    @JsonProperty("ktQuestionsCount")
    @Builder.Default
    private Integer ktQuestionsCount = 5;

    @Column(name = "kt_score")
    @JsonProperty("ktScore")
    private Integer ktScore;

    @JsonProperty("ktAnswers")
    @Column(name = "kt_answers", columnDefinition = "TEXT")
    private String ktAnswers;

    @Column(name = "kt_completed_ts")
    @JsonProperty("ktCompletedTs")
    private LocalDateTime ktCompletedTs;

//    @Column(name = "bm_gps_latitude")
//    @JsonProperty("bmGpsLatitude")
//    private BigDecimal bmGpsLatitude;
//
//    @JsonProperty("bmGpsLongitude")
//    @Column(name = "bm_gps_longitude")
//    private BigDecimal bmGpsLongitude;

//    @JsonProperty("bmGpsAccuracy")
//    @Column(name = "bm_gps_accuracy")
//    private BigDecimal bmGpsAccuracy;

//    @JsonProperty("kmGpsLatitude")
//    @Column(name = "km_gps_latitude")
//    private BigDecimal kmGpsLatitude;

//    @JsonProperty("kmGpsLongitude")
//    @Column(name = "km_gps_longitude")
//    private BigDecimal kmGpsLongitude;

    @JsonProperty("gpsDistanceBmKm")
    @Column(name = "gps_distance_bm_km")
    private BigDecimal gpsDistanceBmKm;

    @JsonProperty("gpsMismatchFlag")
    @Column(name = "gps_mismatch_flag")
    @Builder.Default
    private Character gpsMismatchFlag = 'N';

    @JsonProperty("distFromKendraM")
    @Column(name = "dist_from_kendra_m")
    private BigDecimal distFromKendraM;

    @JsonProperty("distFromKendraFlag")
    @Column(name = "dist_from_kendra_flag")
    @Builder.Default
    private Character distFromKendraFlag = 'N';

    @JsonProperty("locationCapturedTs")
    @Column(name = "location_captured_ts")
    private LocalDateTime locationCapturedTs;

    @JsonProperty("housePhotoDocId")
    @Column(name = "house_photo_doc_id")
    private String housePhotoDocId;

    @JsonProperty("housePhotoClarity")
    @Column(name = "house_photo_clarity")
    private BigDecimal housePhotoClarity;

    @JsonProperty("housePhotoClarityPass")
    @Column(name = "house_photo_clarity_pass")
    private Character housePhotoClarityPass;

    @JsonProperty("housePhotoTs")
    @Column(name = "house_photo_ts")
    private LocalDateTime housePhotoTs;

    @JsonProperty("loanEditedByBm")
    @Column(name = "loan_edited_by_bm")
    @Builder.Default
    private Character loanEditedByBm = 'N';

    @JsonProperty("questionnaireAnswers")
    @Column(name = "questionnaire_answers", columnDefinition = "TEXT")
    private String questionnaireAnswers;

    @JsonProperty("decision")
    @Column(name = "decision")
    private String decision;

    @JsonProperty("rejectionReasons")
    @Column(name = "rejection_reasons", columnDefinition = "TEXT")
    private String rejectionReasons;

    @JsonProperty("decisionRemarks")
    @Column(name = "decision_remarks")
    private String decisionRemarks;

    @JsonProperty("decisionTs")
    @Column(name = "decision_ts")
    private LocalDateTime decisionTs;

    @JsonProperty("status")
    @Column(name = "status")
    @Builder.Default
    private String status = "PENDING";    // PENDING / IN_PROGRESS / COMPLETED / REJECTED

    // TEXT. JSON array, one entry per subStage encountered so far, e.g.
    // [{"subStage":"1.1","verified":"Y"},{"subStage":"1.2","verified":"N"}].
    // 1.1 = Document Verification, 1.2 = Knowledge Test, 1.3 = Capture House Location,
    // 1.4 = Capture Member House Photo, 1.5 = Loan Details, 1.6 = Review.
    // verified = "Y" for a real submit (isDraft=false/absent) touching that subStage, "N" for a
    // draft save (isDraft=true) -- see BMReinterviewProcessor#buildSubStagePayload.
    @JsonProperty("subStage")
    @Column(name = "sub_stage", columnDefinition = "TEXT")
    private String subStage;

    @JsonProperty("subStageStatus")
    @Column(name = "sub_stage_status")
    private String subStageStatus;  //    OPENED/CLOSED

    @JsonProperty("reinterviewStartTs")
    @Column(name = "reinterview_start_ts")
    private LocalDateTime reinterviewStartTs;

    @JsonProperty("reinterviewEndTs")
    @Column(name = "reinterview_end_ts")
    private LocalDateTime reinterviewEndTs;

    @JsonProperty("createdTs")
    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @JsonProperty("updatedTs")
    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

    @JsonProperty("updatedBy")
    @Column(name = "updated_by")
    private String updatedBy;
}
