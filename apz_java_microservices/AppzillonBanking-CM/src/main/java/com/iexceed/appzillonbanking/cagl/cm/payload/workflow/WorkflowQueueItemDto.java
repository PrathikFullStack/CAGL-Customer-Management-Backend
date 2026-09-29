package com.iexceed.appzillonbanking.cagl.cm.payload.workflow;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowQueueItemDto {
    private String applicationId;
    private String customerId;
    private String customerName;
    private String mobileNumber;
    private String branchId;
    private String branchName;
    private String kendraId;
    private String kendraName;
    private String groupId;
    private String groupName;
    private String kmId;
    private String kmName;
    private String stage;
    private String status;
    private String requestType;
    private String campaignStart;
    private String pendingRole;
    private Boolean isOffline;
    private Boolean isLocked;
    private String lockedBy;
    private LocalDateTime createdTs;
    private LocalDateTime updatedTs;
}
