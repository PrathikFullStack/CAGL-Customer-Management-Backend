package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.time.Instant;

@Builder
public record AuditEventDto(
        String userId,
        String userName,
        String userRole,
        String stageId,
        String subStage,
        Instant eventTs
) {}