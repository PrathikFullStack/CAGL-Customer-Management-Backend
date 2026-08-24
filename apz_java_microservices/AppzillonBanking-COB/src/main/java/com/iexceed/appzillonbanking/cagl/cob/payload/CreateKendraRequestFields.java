package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKendraRequestFields {

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("kmId")
    private String kmId;

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("state")
    private Integer state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("village")
    private String village;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("gpsLatitude")
    private BigDecimal gpsLatitude;

    @JsonProperty("gpsLongitude")
    private BigDecimal gpsLongitude;

    @JsonProperty("distanceFromBranch")
    private BigDecimal distanceFromBranch;

    @JsonProperty("meetingDay")
    private String meetingDay;

    @JsonProperty("meetingTime")
    private String meetingTime;

    @JsonProperty("meetingPlace")
    private String meetingPlace;

    @JsonProperty("meetingFrequency")
    private String meetingFrequency;

    @JsonProperty("firstMeetingDate")
    private LocalDate firstMeetingDate;

    @JsonProperty("photoDocId")
    private String photoDocId;

    @JsonProperty("dmsFolderIdx")
    private String dmsFolderIdx;


    @JsonProperty("payload")
    private Map<String, Object> payload;

}
