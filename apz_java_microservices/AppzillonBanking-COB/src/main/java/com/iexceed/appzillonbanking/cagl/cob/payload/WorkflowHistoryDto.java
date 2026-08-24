package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.time.Instant;

@Builder
public record WorkflowHistoryDto(
        Integer versionNo,
        Integer workflowSeqNo,
        String applicationStatus,
        Instant createdTs,
        String createdBy,
        String presentRole,
        String nextWorkflowStage,
        String remarks,
        String createdUsername
) {}