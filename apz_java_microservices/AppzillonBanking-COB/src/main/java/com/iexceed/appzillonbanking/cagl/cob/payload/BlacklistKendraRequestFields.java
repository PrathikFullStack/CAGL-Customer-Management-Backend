package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirror of CAGL's BlacklistKendraRequestFields — COB can't import
 * CAGL's class directly (separate deployables).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistKendraRequestFields {

    @NotBlank(message = "kendraId is required")
    @JsonProperty("kendraId")
    private String kendraId;

    @NotBlank(message = "action is required")
    @JsonProperty("action")
    private String action;

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("kendraName")
    private String kendraName;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("village")
    private String village;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("kendraSource")
    private String kendraSource;
}
