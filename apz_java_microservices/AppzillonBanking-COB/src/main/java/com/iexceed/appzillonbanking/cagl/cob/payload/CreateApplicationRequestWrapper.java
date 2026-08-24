package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outer envelope used when this API is itself invoked through the wider
 * Appzillon "apiRequest" gateway convention. The controller accepts the
 * unwrapped {@link CreateApplicationRequest} body directly (matching the
 * payload sample); this wrapper is kept for callers that route through
 * the gateway layer and post the wrapped form instead.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateApplicationRequestWrapper {
    @Valid
    @JsonProperty("apiRequest")
    private CreateApplicationRequest apiRequest;
}
