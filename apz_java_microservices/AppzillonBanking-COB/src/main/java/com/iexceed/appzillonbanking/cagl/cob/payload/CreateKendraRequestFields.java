package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("addressLine3")
    private String addressLine3;

    @JsonProperty("addressLine4")
    private String addressLine4;

    @JsonProperty("state")
    private Integer state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("taluk")
    private String taluk;

    @JsonProperty("village")
    private String village;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("areaType")
    private String areaType;

    @JsonProperty("villageType")
    private String villageType;

    @JsonProperty("gpsLatitude")
    private BigDecimal gpsLatitude;

    @JsonProperty("gpsLongitude")
    private BigDecimal gpsLongitude;

    @JsonProperty("distanceFromBranch")
    private BigDecimal distanceFromBranch;

    @JsonProperty("meetingDay")
    private String meetingDay;

    @JsonProperty("meetingTimeFrom")
    private String meetingTimeFrom;

    @JsonProperty("meetingTimeTo")
    private String meetingTimeTo;

    @JsonProperty("meetingPlace")
    private String meetingPlace;

    @JsonProperty("meetingFrequency")
    private String meetingFrequency;

    @JsonProperty("firstMeetingDate")
    private LocalDate firstMeetingDate;

    @JsonProperty("projectionMeetingDate")
    private LocalDate projectionMeetingDate;

    @JsonProperty("dmsFolderIdx")
    private String dmsFolderIdx;

    @JsonProperty("payload")
    private Map<String, Object> payload;
}
