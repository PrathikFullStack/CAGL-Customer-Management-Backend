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
public class BMReinterviewCustomerDetails {

    @JsonProperty("kycDetails")
    private BMReinterviewKycDetails kycDetails;

    @JsonProperty("docVerified")
    private Boolean docVerified;

    @JsonProperty("documentVerification")
    private List<BMReinterviewDocumentVerificationRequestFields> documentVerification;

    @JsonProperty("knowledgeTest")
    private BMReinterviewKnowledgeTestRequestFields knowledgeTest;

    @JsonProperty("gpsDistanceBmKm")
    private BigDecimal gpsDistanceBmKm;

    @JsonProperty("gpsMisMatchFlag")
    private Character gpsMisMatchFlag;

    @JsonProperty("distanceFromKendra")
    private BigDecimal distanceFromKendra;

    @JsonProperty("distanceFromKendraFlag")
    private Character distanceFromKendraFlag;

    @JsonProperty("locationDetails")
    private List<BMReinterviewLocationDetails> locationDetails;

    @JsonProperty("locationCapturedTs")   // GPS capture timestamp
    private Long locationCapturedTs;

    @JsonProperty("housePhoto")
    private HousePhotoRequestFields housePhoto;

    @JsonProperty("loanEditedByBm")
    private Character loanEditedByBm;

    @JsonProperty("questionnaire")
    private BMReinterviewUnnatiQuestionnaireRequestFields questionnaire;

    @JsonProperty("unnatiRedirect")
    private Character unnatiRedirect;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("rejectionReasons")
    private List<String> rejectionReasons;

    @JsonProperty("decisionRemarks")
    private String decisionRemarks;

}
