package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeAssessmentCardDto {
    private String totalIncome;
    private String totalExpenses;
    private String netSurplus;
    private String sourceOfIncome;
    private String wetLandAcres;
    private String dryLandAcres;
    private String religion;
    private String caste;
    private String nationality;
    private Integer noOfAdults;
    private Integer noOfChildren;
    private String businessPhotoDmsId;
    private String assessmentDate;
}
