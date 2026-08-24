package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Same gateway-wrapped envelope note as CreateApplicationRequestWrapper. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateApplicationRequestWrapper {
    @Valid
    @JsonProperty("apiRequest")
    private UpdateApplicationRequest apiRequest;
}
