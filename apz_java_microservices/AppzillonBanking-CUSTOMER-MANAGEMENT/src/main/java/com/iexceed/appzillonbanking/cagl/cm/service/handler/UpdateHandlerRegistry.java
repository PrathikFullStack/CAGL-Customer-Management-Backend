package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UpdateHandlerRegistry {

    private final Map<String, UpdateHandler> handlerMap = new HashMap<>();

    public UpdateHandlerRegistry(List<UpdateHandler> handlers) {
        for (UpdateHandler handler : handlers) {
            handlerMap.put(handler.getSectionName().toUpperCase(), handler);
        }
    }

    public Optional<UpdateHandler> getHandler(String sectionName) {
        if (sectionName == null) return Optional.empty();
        return Optional.ofNullable(handlerMap.get(sectionName.toUpperCase()));
    }
}
