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
public class WorkflowHistoryDto {
    private String applicationId;
    private Integer versionNo;
    private Integer workflowSeqNo;
    private String applicationStatus;
    private String presentRole;
    private String nextWorkflowStage;
    private String remarks;
    private String createdBy;
    private String createdUsername;
    private LocalDateTime createdTs;
}
