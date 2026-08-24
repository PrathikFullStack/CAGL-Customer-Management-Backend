package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardCountDTO {
    private String status;
    private String stage;
    private String recordType;
    private String channelType;
    private String createdBy;
    private String wfStage;
    private long count;

    // Overloaded constructor for queries that don't include wfStage
    public DashboardCountDTO(String status, String stage, String recordType, String channelType, String createdBy, long count) {
        this(status, stage, recordType, channelType, createdBy, null, count);
    }
}
