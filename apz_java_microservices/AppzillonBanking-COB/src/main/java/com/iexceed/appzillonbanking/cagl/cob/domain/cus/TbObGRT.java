package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tb_ob_grt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbObGRT {

    @Id
    @Column(name = "grt_id", nullable = false)
    @JsonProperty("grtId")
    private String grtId;

    @Column(name = "group_id", nullable = false, unique = true)
    @JsonProperty("groupId")
    private String groupId;

    @Column(name = "kendra_id", nullable = false)
    @JsonProperty("kendraId")
    private String kendraId;

    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private String userId;

    @Column(name = "user_role")
    @JsonProperty("userRole")
    private String userRole;

    @Column(name = "am_name")
    @JsonProperty("amName")
    private String amName;

//    @JdbcTypeCode(SqlTypes.JSON)  // TEXT
//    @Column(name = "attendance", columnDefinition = "jsonb")
    @Column(name = "attendance", columnDefinition = "TEXT")
    @JsonProperty("attendance")
//    private List<Map<String, Object>> attendance;
    private String attendance;

    @Column(name = "total_members")
    @JsonProperty("totalMembers")
    private Integer totalMembers;

    @Column(name = "present_count")
    @JsonProperty("presentCount")
    private Integer presentCount;

    @Column(name = "absent_count")
    @JsonProperty("absentCount")
    private Integer absentCount;

    @Column(name = "group_photo_doc_id")
    @JsonProperty("groupPhotoDocId")
    private String groupPhotoDocId;

    @Column(name = "group_photo_clarity", precision = 5, scale = 2)
    @JsonProperty("groupPhotoClarity")
    private BigDecimal groupPhotoClarity;

//    @JdbcTypeCode(SqlTypes.JSON)  //TEXT
//    @Column(name = "doc_verified_members", columnDefinition = "jsonb")
    @Column(name = "doc_verified_members", columnDefinition = "TEXT")
    @JsonProperty("docVerifiedMembers")
//    private List<Map<String, Object>> docVerifiedMembers;
    private String docVerifiedMembers;

    @Column(name = "doc_verification_count")
    @JsonProperty("docVerificationCount")
    private Integer docVerificationCount;

//    @JdbcTypeCode(SqlTypes.JSON) // TEXT
//    @Column(name = "housevisit", columnDefinition = "jsonb")
    @Column(name = "housevisit", columnDefinition = "TEXT")
    @JsonProperty("houseVisit")
//    private List<Map<String, Object>> houseVisit;
    private String houseVisit; // jsonb -> text

    @Column(name = "house_visit_count")
    @JsonProperty("houseVisitCount")
    private Integer houseVisitCount;

    @Column(name = "house_visit_pct", precision = 5, scale = 2)
    @JsonProperty("houseVisitPct")
    private BigDecimal houseVisitPct;

//    @JdbcTypeCode(SqlTypes.JSON) // TEXT
//    @Column(name = "ln_review", columnDefinition = "jsonb")
    @Column(name = "ln_review", columnDefinition = "TEXT")
    @JsonProperty("lnReview")
//    private Map<String, Object> lnReview;
    private String lnReview;

//    @Column(name = "loan_edited_by_am", nullable = false)
//    @JsonProperty("loanEditedByAm")
//    private Character loanEditedByAm;

//    @Column(name = "bre_triggered", nullable = false)
//    @JsonProperty("breTriggered")
//    private Character breTriggered;

//    @Column(name = "bre_status")
//    @JsonProperty("breStatus")
//    private String breStatus;

//    @Column(name = "bre_eligible_amt", precision = 12, scale = 2)
//    @JsonProperty("breEligibleAmt")
//    private BigDecimal breEligibleAmt;

//    @Column(name = "unnati_eligible", nullable = false)
//    @JsonProperty("unnatiEligible")
//    private Character unnatiEligible;

//    @Column(name = "cb_validity_check")
//    @JsonProperty("cbValidityCheck")
//    private Character cbValidityCheck;

//    @JdbcTypeCode(SqlTypes.JSON)  // TEXT
//    @Column(name = "questionnaire_answers", columnDefinition = "jsonb")
    @Column(name = "questionnaire_answers", columnDefinition = "TEXT")
    @JsonProperty("questionnaireAnswers")
//    private List<Map<String, Object>> questionnaireAnswers;
    private String questionnaireAnswers;

    @Column(name = "annexure_doc_id")
    @JsonProperty("annexureDocId")
    private String annexureDocId;

    @Column(name = "annexure_clarity", precision = 5, scale = 2)
    @JsonProperty("annexureClarity")
    private BigDecimal annexureClarity;

    @Column(name = "kendra_meeting_day")
    @JsonProperty("kendraMeetingDay")
    private String kendraMeetingDay;

    @Column(name = "kendra_meeting_time")
    @JsonProperty("kendraMeetingTime")
    private String kendraMeetingTime;

    @Column(name = "kendra_meeting_place")
    @JsonProperty("kendraMeetingPlace")
    private String kendraMeetingPlace;

    @Column(name = "gl_customer_id")
    @JsonProperty("glCustomerId")
    private String glCustomerId;

    @Column(name = "gl_mobile")
    @JsonProperty("glMobile")
    private String glMobile;

    @Column(name = "kl_customer_id")
    @JsonProperty("klCustomerId")
    private String klCustomerId;

    @Column(name = "kl_mobile")
    @JsonProperty("klMobile")
    private String klMobile;

    @Column(name = "decision")
    @JsonProperty("decision")
    private String decision;

//    @JdbcTypeCode(SqlTypes.JSON) //TEXT
//    @Column(name = "rejection_reasons", columnDefinition = "jsonb")
    @Column(name = "rejection_reasons", columnDefinition = "TEXT")
    @JsonProperty("rejectionReasons")
//    private List<String> rejectionReasons;
    private String rejectionReasons;

    @Column(name = "decision_remarks")
    @JsonProperty("decisionRemarks")
    private String decisionRemarks;

    @Column(name = "status", nullable = false)
    @JsonProperty("status")
    private String status;

    @Column(name = "sub_stage") // --->> text todo
    @JsonProperty("subStage")
    private String subStage;

    @Column(name = "sub_stage_status")
    @JsonProperty("subStageStatus")
    private String subStageStatus;

    @Column(name = "grt_start_ts")
    @JsonProperty("grtStartTs")
    private LocalDateTime grtStartTs;

    @Column(name = "grt_end_ts")
    @JsonProperty("grtEndTs")
    private LocalDateTime grtEndTs;

    @Column(name = "created_ts", nullable = false)
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;

    @Column(name = "updated_by")
    @JsonProperty("updatedBy")
    private String updatedBy;
}
