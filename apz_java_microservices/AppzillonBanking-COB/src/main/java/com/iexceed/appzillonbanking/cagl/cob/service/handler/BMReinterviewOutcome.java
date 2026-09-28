package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObBMReInterview;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import com.iexceed.appzillonbanking.cagl.cob.service.BMReinterviewService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of {@link BMReinterviewHandler#process} handed back to the {@link BMReinterviewService}
 * facade.
 * <p>
 * When the BM's decision is {@code MOVETOCGT}, the whole group is reverted to the CGT stage instead
 * of the reinterview record being finalized -- in that case {@code revertedToCgt} is {@code true}
 * and {@code reinterview}/{@code kycDetails}/{@code locationDetails}/{@code loanDetails} are
 * irrelevant; the facade builds a "reverted" message from
 * {@code BMReInterviewRequestFields#getGroupId()} instead of rendering them.
 * <p>
 * {@code locationDetails} is the raw JSON text currently stored on
 * {@code tb_ob_customer.location_details} (the BM/KM GPS points captured at subStage 1.3) --
 * {@link BMReinterviewResponseMapper} parses it back into nested JSON at response time, same as it
 * already does for the reinterview entity's own JSON-text columns. {@code loanDetails} is the
 * {@code tb_ob_loan} row for this application (looked up by {@code applicationId}, one loan per
 * application), so the response doesn't require a separate call; it's {@code null} when the
 * application has no loan recorded yet.
 * <p>
 * {@code record} only stops {@code reinterview}/{@code kycDetails}/{@code locationDetails}/
 * {@code loanDetails}/{@code revertedToCgt} from being <i>reassigned</i> -- it says nothing about
 * the objects they point at. {@code kycDetails} is defensively copied into an unmodifiable map
 * below so a caller can't mutate it out from under whoever else holds this outcome. It's a plain
 * {@code Collections.unmodifiableMap} copy rather than {@code Map.copyOf} because KYC fields the BM
 * hasn't captured yet legitimately serialize to {@code null} values (e.g. {@code altMobileNum}),
 * and {@code Map.copyOf} rejects those with an NPE. {@code reinterview}/{@code loanDetails} are
 * deliberately left as-is: they're Hibernate-managed entities for the lifetime of
 * {@link BMReinterviewHandler#process}, and making them immutable would fight the ORM, not this
 * record -- callers past that boundary are expected to only read them (via
 * {@link BMReinterviewResponseMapper}), never mutate them.
 */
public record BMReinterviewOutcome(TbObBMReInterview reinterview, Map<String, Object> kycDetails,
                                   String locationDetails, TbObLoan loanDetails, boolean revertedToCgt) {

    public BMReinterviewOutcome {
        kycDetails = kycDetails == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(kycDetails));
    }
}
