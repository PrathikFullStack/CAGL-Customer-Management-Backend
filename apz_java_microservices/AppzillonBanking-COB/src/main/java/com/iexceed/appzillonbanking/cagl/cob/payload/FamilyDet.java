package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Sub-stage 1.4 - family / household members. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyDet {

    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @JsonProperty("status")
    private String status;

    @JsonProperty("memberList")
    private List<FamilyMemberItem> memberList;
}
