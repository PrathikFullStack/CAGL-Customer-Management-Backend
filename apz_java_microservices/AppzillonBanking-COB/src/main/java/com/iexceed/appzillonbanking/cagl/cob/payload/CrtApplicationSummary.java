package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
public class CrtApplicationSummary {
    private final String applicationId;
    private final String customerName;
    private final String kendraId;
    private final String kendraName;
    private final LocalDateTime createdTs;
    private final LocalDateTime updatedTs;
    private final String status;
    private final long agingDays;

    public CrtApplicationSummary(ApplicationSummary summary, long agingDays) {
        this.applicationId = summary.getApplicationId();
        this.customerName = summary.getCustomerName();
        this.kendraId = summary.getKendraId();
        this.kendraName = summary.getKendraName();
        this.createdTs = summary.getCreatedTs();
        this.updatedTs = summary.getUpdatedTs();
        this.status = summary.getStatus();
        this.agingDays = agingDays;
    }
}