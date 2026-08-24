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

/** Substage 1.1 - mobile/OTP/consent capture. Merges into customer.payload. */
@Component
public class OtpConsentUpdateHandler implements UpdateHandler {

    @Override
    public UpdateType supports() {
        return UpdateType.OTP_CONSENT;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        List<String> changed = new ArrayList<>();
        Map<String, Object> payload = new HashMap<>(ctx.customer().getPayload() != null
                ? ctx.customer().getPayload() : Map.of());

        mergeIfPresent(p, "mobileNum", payload, "mobileNumber", changed);
        mergeIfPresent(p, "alterMobileNum", payload, "alterMobileNumber", changed);
        mergeIfPresent(p, "language", payload, "language", changed);
        mergeIfPresent(p, "deviceType", payload, "deviceType", changed);

        ctx.customer().setPayload(payload);
        String mobile = JsonNodeUtil.text(p, "mobileNum");
//        if (mobile != null && JsonNodeUtil.differs(ctx.customer().getMobileNumber(), mobile)) {
//            ctx.customer().setMobileNumber(mobile);
//            changed.add("mobileNumber");
//        }

        return HandlerResult.of(changed, "1.1");
    }

    private void mergeIfPresent(JsonNode source, String sourceField, Map<String, Object> target,
                                 String targetField, List<String> changed) {
        String value = JsonNodeUtil.text(source, sourceField);
        if (value == null) {
            return;
        }
        if (JsonNodeUtil.differs(target.get(targetField), value)) {
            target.put(targetField, value);
            changed.add(targetField);
        }
    }
}
