package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistanceValidationResult {

    private BigDecimal distanceKm;
    private BigDecimal thresholdKm;
    private BigDecimal deviationKm;
    private boolean exceeded;
}
