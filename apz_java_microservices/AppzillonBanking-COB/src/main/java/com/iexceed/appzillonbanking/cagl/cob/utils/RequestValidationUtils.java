package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.iexceed.appzillonbanking.cagl.cob.payload.CreateApplicationRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateLeadRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.ApplicationCreateDtls;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized null/blank validation for inbound request payloads, run before
 * any duplicate-check or persistence logic executes.
 */
public final class RequestValidationUtils {

    private RequestValidationUtils() {
    }

    /**
     * Validates a CreateApplicationRequestFields payload.
     * @return list of validation error messages; empty list means valid.
     */
    public static List<String> validateApplicationRequest(CreateApplicationRequestFields requestObj) {
        List<String> errors = new ArrayList<>();

        if (requestObj == null) {
            errors.add("Request body is missing.");
            return errors; // nothing further can be checked
        }

        if (CollectionUtils.isEmpty(requestObj.getApplicationdtls())) {
            errors.add("applicationdtls is missing or empty.");
            return errors;
        }

        int index = 0;
        for (ApplicationCreateDtls record : requestObj.getApplicationdtls()) {
            String prefix = "applicationdtls[" + index + "]";

            if (record == null) {
                errors.add(prefix + " is null.");
                index++;
                continue;
            }
            if (record.getCustomerDtls() == null) {
                errors.add(prefix + ".customerDtls is missing.");
            } else if (record.getCustomerDtls().getKycDetails() == null) {
                errors.add(prefix + ".customerDtls.kycDetails is missing.");
            } else if (!StringUtils.hasText(record.getCustomerDtls().getKycDetails().getMobileNum())) {
                errors.add(prefix + ".customerDtls.kycDetails.mobileNum is missing.");
            } else if (!isValidMobileNumber(record.getCustomerDtls().getKycDetails().getMobileNum())) {
                errors.add(prefix + ".customerDtls.kycDetails.mobileNum is invalid.");
            }
            index++;
        }
        return errors;
    }

    /**
     * Validates a CreateLeadRequestFields payload.
     */
    public static List<String> validateLeadRequest(CreateLeadRequestFields requestObj) {
        List<String> errors = new ArrayList<>();

        if (requestObj == null) {
            errors.add("Request body is missing.");
            return errors;
        }

        if (CollectionUtils.isEmpty(requestObj.getRecords())) {
            errors.add("records is missing or empty.");
            return errors;
        }

        int index = 0;
        for (CreateLeadRequestFields.LeadRecord record : requestObj.getRecords()) {
            String prefix = "records[" + index + "]";

            if (record == null) {
                errors.add(prefix + " is null.");
            } else if (!StringUtils.hasText(record.getMemberMobile())) {
                errors.add(prefix + ".memberMobile is missing.");
            } else if (!isValidMobileNumber(record.getMemberMobile())) {
                errors.add(prefix + ".memberMobile is invalid.");
            }
            index++;
        }
        return errors;
    }

    /** Basic 10-digit Indian mobile number check — adjust regex to your actual business rule. */
    public static boolean isValidMobileNumber(String mobileNum) {
        return StringUtils.hasText(mobileNum) && mobileNum.matches("^[6-9]\\d{9}$");
    }
}
