package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObBMReInterview;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import com.iexceed.appzillonbanking.cagl.cob.payload.BMReInterviewRequestFields;

import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shapes the {@code responseObj} for a successful BM Reinterview submission -- kept separate from
 * {@link BMReinterviewHandler} so the persistence/transaction concern doesn't also have to know
 * how the client wants the payload rendered.
 */
@Component
@RequiredArgsConstructor
public class BMReinterviewResponseMapper {

    private static final Logger logger = LogManager.getLogger(BMReinterviewResponseMapper.class);

    private final ObjectMapper objectMapper;

    /**
     * Returns the whole persisted {@code tb_ob_bm_reinterview} record (every column, via the
     * entity's own {@code @JsonProperty} names) rather than a hand-picked subset of fields --
     * nested under its own {@code bmReinterview} node -- plus a friendly {@code message}, the
     * {@code isDraft} flag this call was made with, the customer's {@code kyc}/{@code location}
     * nodes (which live on {@code tb_ob_customer}, not this entity), and the {@code loan} node for
     * this application (from {@code tb_ob_loan}, {@code null} if none recorded yet). TEXT columns
     * that store JSON ({@code docVerifyPayload}/{@code ktAnswers}/{@code questionnaireAnswers}/
     * {@code rejectionReasons}/{@code subStage}/{@code locationDetails}) are re-parsed so they
     * render as nested JSON in the response instead of escaped strings.
     */
    public String toResponseJson(TbObBMReInterview bmReInterview, BMReInterviewRequestFields requestObj,
                                 Map<String, Object> kycDetails, String locationDetails, TbObLoan loanDetails) {

        logger.info("Building BM ReInterview response JSON -- reinterviewId : {}, applicationId : {}",
                bmReInterview.getReinterviewId(), bmReInterview.getApplicationId());
        try {
            Map<String, Object> bmReinterviewMap =
                    objectMapper.convertValue(bmReInterview, new TypeReference<Map<String, Object>>() {});

            bmReinterviewMap.put("docVerifyPayload", parseJsonOrNull(bmReInterview.getDocVerifyPayload()));
            bmReinterviewMap.put("ktAnswers", parseJsonOrNull(bmReInterview.getKtAnswers()));
            bmReinterviewMap.put("questionnaireAnswers", parseJsonOrNull(bmReInterview.getQuestionnaireAnswers()));
            bmReinterviewMap.put("rejectionReasons", parseJsonOrNull(bmReInterview.getRejectionReasons()));
            bmReinterviewMap.put("subStage", parseJsonOrNull(bmReInterview.getSubStage()));

            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("message", "BM ReInterview submitted successfully");
            responseMap.put("bmReInterviewDetails", bmReinterviewMap);
            responseMap.put("kycDetails", kycDetails);
            responseMap.put("loanDetails", loanDetails);
            responseMap.put("locationDetails", parseJsonOrNull(locationDetails));

            responseMap.put("isDraft", Boolean.TRUE.equals(requestObj.getIsDraft()));
            String json = objectMapper.writeValueAsString(responseMap);
            logger.debug("BM ReInterview response JSON built for reinterviewId : {} : {}", bmReInterview.getReinterviewId(), json);
            return json;
        } catch (Exception ex) {
            logger.error("Error while preparing BM response for reinterviewId : {}", bmReInterview.getReinterviewId(), ex);
            throw new RuntimeException("Unable to prepare response.");
        }
    }

    /**
     * Parses a TEXT column storing arbitrary JSON (array of objects, array of strings, etc.) back
     * into its natural Java form so it renders as nested JSON in the response instead of an
     * escaped string. Returns {@code null} for a null/blank value rather than an empty collection.
     */
    private Object parseJsonOrNull(String json) {

        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing stored JSON value.", e);
        }
    }
}
