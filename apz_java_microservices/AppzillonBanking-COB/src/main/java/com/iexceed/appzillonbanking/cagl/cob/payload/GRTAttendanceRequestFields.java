package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GRTAttendanceRequestFields {

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("present")
    private Boolean present;

    @JsonProperty("absentReason")
    private String absentReason;
}
