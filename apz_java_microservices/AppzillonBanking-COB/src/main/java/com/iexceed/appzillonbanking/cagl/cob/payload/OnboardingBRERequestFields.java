package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingBRERequestFields {

    @JsonProperty("household_member")
    private List<HouseholdMemberPayload> household_member;

    @JsonProperty("applicant")
    private Applicant applicant;

}
