package com.iexceed.appzillonbanking.cagl.cob.service.resolver;

import com.iexceed.appzillonbanking.cagl.cob.exception.UnsupportedRoleException;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.FIFetchApplicationHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.FetchApplicationHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.KMFetchApplicationHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FetchApplicationHandlerResolver {

    private final Map<String, FetchApplicationHandler> handlersByRole;

    public FetchApplicationHandlerResolver(KMFetchApplicationHandler km, FIFetchApplicationHandler fi) {
        this.handlersByRole = Map.of(
                "KM", km,
                "BM", fi
        );
    }

    public FetchApplicationHandler resolve(String userRole) {
        FetchApplicationHandler handler = handlersByRole.get(userRole);
        if (handler == null) {
            throw new UnsupportedRoleException(userRole);
        }
        return handler;
    }
}