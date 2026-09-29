package com.iexceed.appzillonbanking.cagl.cm.payload.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmDashboardSummaryDto {

    @JsonProperty("actionRequired")
    private ActionRequiredCounts actionRequired;

    @JsonProperty("overview")
    private OverviewCounts overview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequiredCounts {
        private long drafts;
        private long draftsOnline;
        private long draftsOffline;
        private long onhold;
        private long campaignDrive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewCounts {
        private long pendingBmReview;
        private long pendingAmReview;
        private long pendingRpcReview;
        private long completed;
        private long rejected;
        private long photoDedupePending;
        private long t24UpdationPending;
        private long t24UpdationFailed;
    }
}
