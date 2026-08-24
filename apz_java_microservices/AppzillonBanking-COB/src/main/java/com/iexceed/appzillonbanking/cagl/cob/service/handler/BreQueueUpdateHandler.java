package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLoanRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Substage 2.1 - BRE response capture. Updates tb_ob_loan and customer.breStatus/amlStatus. */
@Component
@RequiredArgsConstructor
public class BreQueueUpdateHandler implements UpdateHandler {

    private final TbObLoanRepository loanRepository;

    @Override
    public UpdateType supports() {
        return UpdateType.BRE_QUEUE;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        List<String> changed = new ArrayList<>();

        String breStatus = JsonNodeUtil.text(p, "breStatus");
//        if (breStatus != null && JsonNodeUtil.differs(ctx.customer().getBreStatus(), breStatus)) {
//            ctx.customer().setBreStatus(breStatus);
//            changed.add("breStatus");
//        }

        Map<String, Object> payload = new HashMap<>(ctx.customer().getPayload() != null
                ? ctx.customer().getPayload() : Map.of());
        for (String field : List.of("overDueAmount", "writtenoffAmount", "indebtedness",
                "foirAmount", "finalFior", "breDate", "reason")) {
            String value = JsonNodeUtil.text(p, field);
            if (value != null) {
                payload.put(field, value);
            }
        }
        ctx.customer().setPayload(payload);

        loanRepository.findByApplicationIdAndCustomerId(ctx.applicationId(), ctx.customerId()).ifPresent(loan -> {
            String loanAmount = JsonNodeUtil.text(p, "loanAmount");
            if (loanAmount != null && !loanAmount.isBlank()) {
                loan.setAmount(new BigDecimal(loanAmount));
            }
            loan.setBreResponseStatus(breStatus);
            loan.setBreTriggerPoint(1); // 1 = KM Submit, per schema remark on bre_trigger_point
            loan.setUpdatedTs(ctx.nowEpochMillis());
            loanRepository.save(loan);
        });

        String nextStatus = "REJECTED".equalsIgnoreCase(breStatus) ? "DRAFTREJECT"
                : "CB_QUEUE".equalsIgnoreCase(breStatus) ? "CB_QUEUE"
                : breStatus != null ? "RPC_PENDING" : null;

        return HandlerResult.of(changed, "2.1", nextStatus);
    }
}
