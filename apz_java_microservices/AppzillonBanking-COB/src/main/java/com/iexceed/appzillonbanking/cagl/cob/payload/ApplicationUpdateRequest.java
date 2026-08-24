package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Envelope for PUT /application/update. {@code payload} is intentionally a
 * raw {@link JsonNode} - its shape depends on {@code updateType} and is
 * bound to a concrete DTO only inside the matching {@code UpdateHandler}.
 * This keeps the controller/service layer agnostic of the ~12 different
 * sub-schemas described in the FSD.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateRequest {

    @NotBlank(message = "applicationId is required")
    private String applicationId;

    @NotNull(message = "updateType is required")
    private UpdateType updateType;

    /** Sub-stage the client believes it is submitting, e.g. "1.4". Optional - server is authoritative. */
    private String subStage;

    @NotBlank(message = "updatedBy is required")
    private String updatedBy;

    @NotBlank(message = "updatedByRole is required")
    private String updatedByRole;

    /**
     * Client's last-seen updated_ts, used for optimistic-concurrency check.
     * Null is treated as "skip check" (e.g. first-ever save on a substage).
     */
    private Long expectedUpdatedTs;

    @NotNull(message = "payload is required")
    private JsonNode payload;
}