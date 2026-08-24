package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GRTSubmitRequestFields {

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("amId")
    private String amId;

    @JsonProperty("amName")
    private String amName;

    @JsonProperty("attendance")
    private List<GRTAttendanceRequestFields> attendance;

    @JsonProperty("groupPhotoDocId")
    private String groupPhotoDocId;

    @JsonProperty("groupPhotoClarity")
    private BigDecimal groupPhotoClarity;

    @JsonProperty("documentVerification")
    private List<GRTDocumentVerificationRequestFields> documentVerification;

    @JsonProperty("houseVisit")
    private List<GRTHouseVisitRequestFields> houseVisit;

    @JsonProperty("houseVisitCount")
    private Integer houseVisitCount;

    @JsonProperty("houseVisitPct")
    private BigDecimal houseVisitPct;

    @JsonProperty("loanReview")
    private GRTLoanReviewRequestFields loanReview;

    @JsonProperty("loanEditedByAm")
    private Character loanEditedByAm;

    @JsonProperty("breTriggered")
    private Character breTriggered;

    @JsonProperty("breStatus")
    private String breStatus;

    @JsonProperty("breEligibleAmt")
    private BigDecimal breEligibleAmt;

    @JsonProperty("unnatiEligible")
    private Character unnatiEligible;

    @JsonProperty("cbValidityCheck")
    private Character cbValidityCheck;

    @JsonProperty("questionnaire")
    private List<GRTQuestionnaireAnswerRequestFields> questionnaire;

    @JsonProperty("annexureDocId")
    private String annexureDocId;

    @JsonProperty("annexureClarity")
    private BigDecimal annexureClarity;

    @JsonProperty("kendraMeetingDay")
    private String kendraMeetingDay;

    @JsonProperty("kendraMeetingTime")
    private String kendraMeetingTime;

    @JsonProperty("kendraMeetingPlace")
    private String kendraMeetingPlace;

    @JsonProperty("glCustomerId")
    private String glCustomerId;

    @JsonProperty("klCustomerId")
    private String klCustomerId;

    @JsonProperty("glMobile")
    private String glMobile;

    @JsonProperty("klMobile")
    private String klMobile;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("rejectionReasons")
    private List<String> rejectionReasons;

    @JsonProperty("decisionRemarks")
    private String decisionRemarks;

    @JsonProperty("status")
    private String status;

    @JsonProperty("isDraft")
    private Boolean isDraft;

    @JsonProperty("subStage")
    private String subStage;

    @JsonProperty("subStageStatus")
    private String subStageStatus;

    @JsonProperty("grtStartTs")
    private Long grtStartTs;

    @JsonProperty("grtEndTs")
    private Long grtEndTs;

}
