package com.iexceed.appzillonbanking.cagl.cm.payload.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmDashboardSummaryDto {

    private ActionRequiredDto actionRequired;
    private OverviewDto overview;
    private DashboardDataDto data;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequiredDto {
        private DraftsMetricsDto drafts;
        private OnholdMetricsDto onhold;
        private CampaignMetricsDto campaignDrive;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftsMetricsDto {
        private long total;
        private long online;
        private long offline;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnholdMetricsDto {
        private long total;
        private long fromBm;
        private long fromAm;
        private long fromRpc;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignMetricsDto {
        private long total;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewDto {
        private long pendingBmReview;
        private long pendingAmReview;
        private long pendingRpcReview;
        private long completed;
        private long rejected;
        private long photoDedupePending;
        private long t24UpdationPending;
        private long t24UpdationFailed;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardDataDto {
        private DraftsDataDto drafts;
        private OnholdDataDto onhold;
        private List<DashboardMemberItemDto> campaignDrive;
        private OverviewDataDto overview;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftsDataDto {
        private List<DashboardMemberItemDto> all;
        private List<DashboardMemberItemDto> online;
        private List<DashboardMemberItemDto> offline;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnholdDataDto {
        private List<DashboardMemberItemDto> all;
        private List<DashboardMemberItemDto> fromBm;
        private List<DashboardMemberItemDto> fromAm;
        private List<DashboardMemberItemDto> fromRpc;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewDataDto {
        private List<DashboardMemberItemDto> pendingBmReview;
        private List<DashboardMemberItemDto> pendingAmReview;
        private List<DashboardMemberItemDto> pendingRpcReview;
        private List<DashboardMemberItemDto> completed;
        private List<DashboardMemberItemDto> rejected;
        private List<DashboardMemberItemDto> photoDedupePending;
        private List<DashboardMemberItemDto> t24UpdationPending;
        private List<DashboardMemberItemDto> t24UpdationFailed;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMemberItemDto {
        private String applicationId;
        private String memberId;
        private String memberName;
        private String mobileNumber;
        private String kmId;
        private String kmName;
        private String kendraId;
        private String kendraName;
        private String groupId;
        private String groupName;
        private String branchId;
        private String branchName;
        private String requestType;
        private String campaignStart;
        private String stage;
        private String subStage;
        private String status;
        private String onholdSource;
        private String channel;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
    }
}
