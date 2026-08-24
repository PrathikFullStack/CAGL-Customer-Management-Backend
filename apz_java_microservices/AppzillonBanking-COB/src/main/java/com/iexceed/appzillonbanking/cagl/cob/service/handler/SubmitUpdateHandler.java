package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import com.iexceed.appzillonbanking.cagl.cob.enums.ApplicationStatus;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLoanRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * End of Stage 1 - captures/updates the loan request (tb_ob_loan) and moves
 * the application to SUBMITTED so it lands in the BRE queue (stage 2).
 */
@Component
@RequiredArgsConstructor
public class SubmitUpdateHandler implements UpdateHandler {

    private final TbObLoanRepository loanRepository;
    private final ObjectMapper objectMapper;

    @Override
    public UpdateType supports() {
        return UpdateType.SUBMIT;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        List<String> changed = new ArrayList<>();

        TbObLoan loan = loanRepository.findByApplicationIdAndCustomerId(ctx.applicationId(), ctx.customerId())
                .orElseGet(() -> TbObLoan.builder()
                        .applicationId(ctx.applicationId())
                        .customerId(ctx.customerId())
                        .loanStatus("INITIATE")
                        .createdTs(ctx.nowEpochMillis())
                        .build());

        JsonNode loanDtls = p.has("loanDtls") && p.get("loanDtls").isArray() && !p.get("loanDtls").isEmpty()
                ? p.get("loanDtls").get(0) : null;

        if (loanDtls != null) {
            applyText(loanDtls, "amount", loan.getAmount(), loan::setAmount, "amount", changed);
            applyText(loanDtls, "approvedAmt", loan.getApprovedAmt(), loan::setApprovedAmt, "approvedAmt", changed);
            String freq = JsonNodeUtil.text(loanDtls, "freq");
            if (freq != null) {
                loan.setFreq(freq);
            }
            String term = JsonNodeUtil.text(loanDtls, "term");
            if (term != null) {
                loan.setTerm(term);
            }
            String product = JsonNodeUtil.text(loanDtls, "product");
            if (product != null) {
                loan.setProduct(product);
            }
            loan.setProductDetails(JsonNodeUtil.asMap(objectMapper, loanDtls.get("purpose")));
            loan.setCharges(java.util.Map.of(
                    "interestRate", JsonNodeUtil.text(loanDtls, "interestRate", ""),
                    "mem_insu", JsonNodeUtil.text(loanDtls, "mem_insu", ""),
                    "sp_insu", JsonNodeUtil.text(loanDtls, "sp_insu", "")));
        }

        loan.setUpdatedTs(ctx.nowEpochMillis());
        loanRepository.save(loan);

        changed.add("status");
        return HandlerResult.of(changed, "1.9", ApplicationStatus.SUBMITTED.name());
    }

    private void applyText(JsonNode node, String field, BigDecimal current,
                            java.util.function.Consumer<BigDecimal> setter, String label, List<String> changed) {
        String raw = JsonNodeUtil.text(node, field);
        if (raw == null || raw.isBlank()) {
            return;
        }
        BigDecimal value = new BigDecimal(raw);
        if (current == null || value.compareTo(current) != 0) {
            setter.accept(value);
            changed.add("loan." + label);
        }
    }
}
