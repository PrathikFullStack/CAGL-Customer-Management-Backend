package com.iexceed.appzillonbanking.cagl.cm.payload.workflow;

import java.time.LocalDateTime;
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
