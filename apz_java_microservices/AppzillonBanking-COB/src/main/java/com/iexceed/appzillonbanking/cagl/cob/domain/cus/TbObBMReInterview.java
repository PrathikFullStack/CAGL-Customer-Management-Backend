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
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reinterview_id", nullable = false)
    @JsonProperty("reinterviewId")
//    private Long reinterviewId;
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
    private Boolean docVerified;

    @Column(name = "doc_verified_ts")
    @JsonProperty("docVerifiedTs")
//    private Long docVerifiedTs;
    private LocalDateTime docVerifiedTs;

    @JsonProperty("docVerifyPayload")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "doc_verify_payload", columnDefinition = "jsonb")
    @Column(name = "doc_verify_payload", columnDefinition = "TEXT")
//    private List<Map<String, Object>> docVerifyPayload;
    private String docVerifyPayload;

    @Column(name = "kt_questions_count")
    @JsonProperty("ktQuestionsCount")
    private Integer ktQuestionsCount;

    @Column(name = "kt_score")
    @JsonProperty("ktScore")
    private Integer ktScore;

    @JsonProperty("ktAnswers")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "kt_answers", columnDefinition = "jsonb")
    @Column(name = "kt_answers", columnDefinition = "TEXT")
//    private List<Map<String, Object>> ktAnswers;
    private String ktAnswers;

    @Column(name = "kt_completed_ts")
    @JsonProperty("ktCompletedTs")
    private LocalDateTime ktCompletedTs;

    @Column(name = "bm_gps_latitude")
    @JsonProperty("bmGpsLatitude")
    private BigDecimal bmGpsLatitude;

    @JsonProperty("bmGpsLongitude")
    @Column(name = "bm_gps_longitude")
    private BigDecimal bmGpsLongitude;

    @JsonProperty("bmGpsAccuracy")
    @Column(name = "bm_gps_accuracy")
    private BigDecimal bmGpsAccuracy;

    @JsonProperty("kmGpsLatitude")
    @Column(name = "km_gps_latitude")
    private BigDecimal kmGpsLatitude;

    @JsonProperty("kmGpsLongitude")
    @Column(name = "km_gps_longitude")
    private BigDecimal kmGpsLongitude;

    @JsonProperty("gpsDistanceBmKm")
    @Column(name = "gps_distance_bm_km")
    private BigDecimal gpsDistanceBmKm;

    @JsonProperty("gpsMismatchFlag")
    @Column(name = "gps_mismatch_flag")
    private Character gpsMismatchFlag;

    @JsonProperty("distFromKendraM")
    @Column(name = "dist_from_kendra_m")
    private BigDecimal distFromKendraM;

    @JsonProperty("distFromKendraFlag")
    @Column(name = "dist_from_kendra_flag")
    private Character distFromKendraFlag;

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
    private Character loanEditedByBm;

//    @JsonProperty("breTriggered")
//    @Column(name = "bre_triggered")
//    private Character breTriggered;
//
//    @JsonProperty("breStatus")
//    @Column(name = "bre_status")
//    private String breStatus;
//
//    @JsonProperty("breEligibleAmt")
//    @Column(name = "bre_eligible_amt")
//    private BigDecimal breEligibleAmt;
//
//    @JsonProperty("cbValidityExceeded")
//    @Column(name = "cb_validity_exceeded")
//    private Character cbValidityExceeded;
//
//    @JsonProperty("unnatiEligible")
//    @Column(name = "unnati_eligible")
//    private Character unnatiEligible;

    @JsonProperty("questionnaireAnswers")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "questionnaire_answers", columnDefinition = "jsonb")
    @Column(name = "questionnaire_answers", columnDefinition = "TEXT")
//    private List<Map<String, Object>> questionnaireAnswers;
    private String questionnaireAnswers;

//    @JsonProperty("unnatiRedirect")
//    @Column(name = "unnati_redirect")
//    private Character unnatiRedirect;

    @JsonProperty("decision")
    @Column(name = "decision")
    private String decision;

    @JsonProperty("rejectionReasons")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "rejection_reasons", columnDefinition = "jsonb")
    @Column(name = "rejection_reasons", columnDefinition = "TEXT")
//    private List<String> rejectionReasons;
    private String rejectionReasons;

    @JsonProperty("decisionRemarks")
    @Column(name = "decision_remarks")
    private String decisionRemarks;

    @JsonProperty("decisionTs")
    @Column(name = "decision_ts")
    private LocalDateTime decisionTs;

//    @JsonProperty("transferDone")
//    @Column(name = "transfer_done")
//    private Character transferDone;
//
//    @JsonProperty("transferFromKendra")
//    @Column(name = "transfer_from_kendra")
//    private String transferFromKendra;
//
//    @JsonProperty("transferFromGroup")
//    @Column(name = "transfer_from_group")
//    private String transferFromGroup;
//
//    @JsonProperty("transferToKendra")
//    @Column(name = "transfer_to_kendra")
//    private String transferToKendra;
//
//    @JsonProperty("transferToGroup")
//    @Column(name = "transfer_to_group")
//    private String transferToGroup;
//
//    @JsonProperty("transferTs")
//    @Column(name = "transfer_ts")
//    private Long transferTs;

    @JsonProperty("status")
    @Column(name = "status")
    private String status;    // PENDING / IN_PROGRESS / COMPLETED / REJECTED

    @JsonProperty("subStage")
    @Column(name = "sub_stage")
    private String subStage;     // 1.1 = Document Verification, 1.2 = Knowledge Test, 1.3 = Capture House Location, 1.4 = Capture Member House Photo, 1.5 = Loan Details

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
