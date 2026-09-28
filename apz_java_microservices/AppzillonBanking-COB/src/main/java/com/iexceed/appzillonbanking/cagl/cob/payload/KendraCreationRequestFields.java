package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KendraCreationRequestFields {

    @JsonProperty("distance")
    private Integer distance;

    @JsonProperty("gps_longitude")
    private String gpsLongitude;

    @JsonProperty("gps_latitude")
    private String gpsLatitude;

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("firstMeetingDate")
    private String firstMeetingDate;

    @JsonProperty("projectionMeetingDate")
    private String projectionMeetingDate;

    @JsonProperty("recordId")
    private String recordId;

    @JsonProperty("searchId")
    private String searchId;

    @JsonProperty("areaType")
    private String areaType;

    @JsonProperty("villageType")
    private String villageType;

    @JsonProperty("KendraAddress")
    private List<KendraAddressItem> kendraAddress;

    @JsonProperty("village")
    private String village;

    @JsonProperty("meetingFrequency")
    private String meetingFrequency;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("surveyDate")
    private String surveyDate;

    @JsonProperty("taluk")
    private String taluk;

    @JsonProperty("recordType")
    private String recordType;

    @JsonProperty("stateId")
    private String stateId;

    @JsonProperty("surveyOfficer")
    private String surveyOfficer;

    @JsonProperty("meetingTimeTo")
    private String meetingTimeTo;

    @JsonProperty("parentId")
    private String parentId;

    @JsonProperty("referenceID")
    private String referenceId;

    @JsonProperty("meetingTimeFrom")
    private String meetingTimeFrom;

    @JsonProperty("kendraManager")
    private String kendraManager;

    @JsonProperty("projectMeetingOfficer")
    private String projectMeetingOfficer;

    @JsonProperty("districtId")
    private String districtId;

    @JsonProperty("meetingPlace")
    private String meetingPlace;

    @JsonProperty("pinCode")
    private Integer pinCode;
}
