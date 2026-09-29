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
public class RecordLockRequestDto {

    @Schema(description = "Application Identifier to acquire or release lock", example = "APP100294")
    private String applicationId;
}
