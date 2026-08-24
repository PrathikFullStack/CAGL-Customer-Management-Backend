package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Substage 1.3 (personal-info slice) - religion/caste/nationality/email/incomeSource -> customer.payload. */
@Component
public class PersonalDetailsUpdateHandler implements UpdateHandler {

    private static final List<String> FIELDS = List.of("religion", "caste", "nationality", "email", "incomeSource");

    @Override
    public UpdateType supports() {
        return UpdateType.PERSONAL_DETAILS;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        Map<String, Object> payload = new HashMap<>(ctx.customer().getPayload() != null
                ? ctx.customer().getPayload() : Map.of());
        List<String> changed = new ArrayList<>();

        for (String field : FIELDS) {
            String value = JsonNodeUtil.text(p, field);
            if (value != null && JsonNodeUtil.differs(payload.get(field), value)) {
                payload.put(field, value);
                changed.add(field);
            }
        }

        ctx.customer().setPayload(payload);
        return HandlerResult.of(changed, "1.3");
    }
}
