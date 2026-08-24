package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;
import java.time.Instant;

@Builder
public record LockInfoDto(
        boolean locked,
        String lockedBy,
        String lockedByRole,
        String lockType,
        String lockedByUsername,
        Instant lockedAt,
        Instant lockExpiresAt,
        Instant lockExpiryTs
) {}