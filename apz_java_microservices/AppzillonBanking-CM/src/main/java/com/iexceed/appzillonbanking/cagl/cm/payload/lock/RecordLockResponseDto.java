package com.iexceed.appzillonbanking.cagl.cm.payload.lock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordLockResponseDto {

    @Schema(description = "Application Identifier", example = "APP100294")
    private String applicationId;

    @Schema(description = "Lock status", example = "LOCKED")
    private String lockStatus;

    @Schema(description = "User who locked the record", example = "8282828230")
    private String lockedBy;

    @Schema(description = "Success status flag", example = "true")
    private boolean success;

    @Schema(description = "Status description message", example = "Lock acquired successfully")
    private String message;
}
