package com.iexceed.appzillonbanking.cagl.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FetchMahiByBranchRequestFields {

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("meetingDate")
    private String meetingDate;

    @JsonProperty("customerId")
    private String customerId;
}
