package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.exception.InvalidUpdateTypeException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the correct {@code UpdateHandler} for a given {@code UpdateType}.
 * All handler beans are injected and self-registered by their {@code supports()}
 * value, so adding a new substage only means adding a new @Component - no
 * change needed here or in the orchestrator.
 */
@Component
public class UpdateHandlerRegistry {

    private final Map<UpdateType, UpdateHandler> handlers = new EnumMap<>(UpdateType.class);

    public UpdateHandlerRegistry(List<UpdateHandler> availableHandlers) {
        // List<UpdateHandler> -> Spring automatically inject the dependency
        for (UpdateHandler handler : availableHandlers) {
            handlers.put(handler.supports(), handler);
        }
    }

    public UpdateHandler resolve(UpdateType updateType) {
        UpdateHandler handler = handlers.get(updateType);
        if (handler == null) {
            throw new InvalidUpdateTypeException("No handler registered for updateType=" + updateType);
        }
        return handler;
    }
}
