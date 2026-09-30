package com.iexceed.appzillonbanking.cagl.cm.payload.update;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    private String customerId;
    private String applicationId;
    private String updateSection;
    private Map<String, Object> updatePayload;
    private String customerConsentOtp;
    private String remarks;
}
