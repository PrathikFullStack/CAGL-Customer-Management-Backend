package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CGTDayDetailsRequestFields {

    @JsonProperty("day")
    private Integer day;

    @JsonProperty("date")
    private String date;

    @JsonProperty("time")
    private String time;

    @JsonProperty("venue")
    private String venue;

    @JsonProperty("groupPhotoId")
    private String groupPhotoId;

    @JsonProperty("annexureId")
    private String annexureId;

    @JsonProperty("memberDetails")
    private List<CGTMemberDetailsRequestFields> memberDetails;

    @JsonProperty("learningSession")
    private List<String> learningSession;

    @JsonProperty("loanDetailsCapture")
    private Boolean loanDetailsCapture;
}
