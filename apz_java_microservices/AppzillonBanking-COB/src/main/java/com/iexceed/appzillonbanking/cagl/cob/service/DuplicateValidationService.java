package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbAsmiUserRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLeadRepository;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateValidationService {

    private static final String STATUS_OPEN = "OPEN";

    private final TbObLeadRepository leadRepository;
    private final TbObApplicationMasterRepository tbObApplicationMasterRepository;
    private final TbAsmiUserRepository tbAsmiUserRepository;
    private final ObjectMapper objectMapper;

    public enum DuplicateCheckScope {
        LEAD_CREATION,
        APPLICATION_CREATION
    }

    public <T> Response validateDatabaseDuplicate(List<T> records,
                                                  Function<T, String> mobileExtractor,
                                                  DuplicateCheckScope scope) throws JsonProcessingException {

        log.info("Inside validateDatabaseDuplicate for scope: {}", scope);

        if (mobileExtractor == null) {
            throw new IllegalArgumentException("mobileExtractor must not be null");
        }
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }

        if (CollectionUtils.isEmpty(records)) {
            log.warn("validateDatabaseDuplicate called with empty/null records list");
            return null;
        }

        for (T record : records) {
            if (record == null) {
                log.warn("Skipping null record in duplicate-check list");
                continue;
            }

            String mobileNum;
            try {
                mobileNum = mobileExtractor.apply(record);
            } catch (Exception ex) {
                // Defensive guard: extractor lambdas chain multiple getters
                // (e.g. record.getCustomerDtls().getKycDetails().getMobileNum());
                // if a caller ever forgets upstream validation, a null in that
                // chain would otherwise NPE here. We fail this record gracefully
                // instead of blowing up the whole request.
                log.error("Failed to extract mobile number from record [{}]: {}", record, ex.getMessage());
                mobileNum = null;
            }

            if (!StringUtils.hasText(mobileNum)) {
                log.warn("Skipping duplicate check — mobile number is null/blank for record: {}", record);
                continue;
            }

            Response duplicateResponse = checkDuplicate(mobileNum, scope);
            if (duplicateResponse != null) {
                return duplicateResponse;
            }
        }
        return null;
    }

    private Response checkDuplicate(String mobileNum, DuplicateCheckScope scope) {

        if (scope == DuplicateCheckScope.LEAD_CREATION) {
            long duplicateLeadCount = leadRepository.countByMobileNumberAndStatus(mobileNum, STATUS_OPEN);
            if (duplicateLeadCount > 0) {
                return buildFailureResponse("Lead already exists.");
            }
        }

        long customerCount = tbObApplicationMasterRepository.countByMobileNumber(mobileNum);
        if (customerCount > 0) {
            return buildFailureResponse("Customer already exists.");
        }

        long asmiUserCount = tbAsmiUserRepository.countByMobileNumber(mobileNum);
        if (asmiUserCount > 0) {
            return buildFailureResponse("User already exists.");
        }

        return null;
    }

    private Response buildFailureResponse(String failureMessage) {
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        CommonUtils.generateHeaderForFailure(responseHeader, failureMessage);

        Response response = new Response();
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
        return response;
    }
}