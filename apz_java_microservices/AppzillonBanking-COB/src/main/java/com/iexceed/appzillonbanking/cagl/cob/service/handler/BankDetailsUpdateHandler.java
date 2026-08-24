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

/** Substage 1.6 - bank account details -> customer.bank_details (penny-drop pending until API 26/external check). */
@Component
public class BankDetailsUpdateHandler implements UpdateHandler {

    @Override
    public UpdateType supports() {
        return UpdateType.BANK_DETAILS;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        Map<String, Object> bank = new HashMap<>(ctx.customer().getBankDetails() != null
                ? ctx.customer().getBankDetails() : Map.of());
        List<String> changed = new ArrayList<>();

        mergeIfPresent(p, "accountNum", bank, "bankAccNo", changed);
        mergeIfPresent(p, "name", bank, "bankAccName", changed);
        mergeIfPresent(p, "ifsc", bank, "bankIfscCode", changed);
        mergeIfPresent(p, "bankDoc", bank, "bankDoc", changed);
        mergeIfPresent(p, "bankDocId", bank, "bankDocId", changed);

        Boolean isPennyCheck = JsonNodeUtil.bool(p, "isPennyCheck");
        if (isPennyCheck != null) {
            bank.put("pennyCheckStatus", isPennyCheck ? "SUCCESS" : "PENDING");
        }

        ctx.customer().setBankDetails(bank);
        return HandlerResult.of(changed, "1.6");
    }

    private void mergeIfPresent(JsonNode source, String sourceField, Map<String, Object> target,
                                 String targetField, List<String> changed) {
        String value = JsonNodeUtil.text(source, sourceField);
        if (value == null) {
            return;
        }
        if (JsonNodeUtil.differs(target.get(targetField), value)) {
            target.put(targetField, value);
            changed.add("bankDetails." + targetField);
        }
    }
}
