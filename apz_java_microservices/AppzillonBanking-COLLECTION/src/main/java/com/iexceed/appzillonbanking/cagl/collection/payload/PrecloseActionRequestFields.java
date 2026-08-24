package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrecloseActionRequestFields {

    @JsonProperty("meetingDate")
    private String meetingDate;

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("branchCode")
    private String branchCode;

}
