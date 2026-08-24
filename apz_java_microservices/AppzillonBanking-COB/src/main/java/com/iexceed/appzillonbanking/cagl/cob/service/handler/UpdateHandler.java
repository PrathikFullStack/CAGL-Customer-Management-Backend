package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;

/**
 * Strategy interface - one implementation per {@link UpdateType}. Keeps
 * ApplicationUpdateServiceImpl free of a giant switch statement and lets
 * each substage's persistence rules evolve independently.
 */
public interface UpdateHandler {

    UpdateType supports();

    /**
     * Apply the update. Implementations must only touch the tables that
     * belong to their substage; the orchestrator owns the shared
     * application_master/customer flag updates and the transaction boundary.
     */
    HandlerResult handle(UpdateContext context);
}
