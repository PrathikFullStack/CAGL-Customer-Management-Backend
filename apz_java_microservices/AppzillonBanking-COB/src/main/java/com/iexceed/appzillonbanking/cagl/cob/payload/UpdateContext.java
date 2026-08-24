package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;

import java.time.LocalDateTime;

/**
 * Everything an {@code UpdateHandler} needs to mutate its slice of the
 * application. The orchestrator loads {@code applicationMaster}/{@code customer}
 * once per request and shares them across handlers to avoid duplicate queries.
 */
public record UpdateContext(
        TbObApplicationMaster applicationMaster,
        TbObCustomer customer,
        JsonNode payload,
        String actorUserId,
        String actorRole,
        LocalDateTime nowEpochMillis
) {
    public String applicationId() {
        return applicationMaster.getApplicationId();
    }

    public String customerId() {
        return customer.getCustomerId();
    }
}