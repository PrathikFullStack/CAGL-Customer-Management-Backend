package com.iexceed.appzillonbanking.cagl.cm.payload.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String kmName;
    private String stage;
    private String status;
    private String pendingRole;
    private LocalDateTime createdTs;
    private LocalDateTime updatedTs;
}
