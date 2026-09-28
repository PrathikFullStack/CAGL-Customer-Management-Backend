package com.iexceed.appzillonbanking.cagl.cm.payload.update;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    private String customerId;
    private String applicationId;
    private String updateSection; // PERSONAL_DETAILS, KYC_DETAILS, ADDRESS, BANK_DETAILS, FAMILY_DETAILS, INCOME_DETAILS, SUBMIT
    private Map<String, Object> updatePayload;
    private String customerConsentOtp;
    private String remarks;
}
