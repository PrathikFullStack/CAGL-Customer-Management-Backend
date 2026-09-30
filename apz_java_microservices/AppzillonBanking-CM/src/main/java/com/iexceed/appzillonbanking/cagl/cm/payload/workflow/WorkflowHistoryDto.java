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
