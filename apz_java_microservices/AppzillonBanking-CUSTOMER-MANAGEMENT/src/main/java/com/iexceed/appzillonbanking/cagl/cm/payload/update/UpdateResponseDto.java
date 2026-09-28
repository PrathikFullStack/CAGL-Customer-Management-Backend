package com.iexceed.appzillonbanking.cagl.cm.payload.update;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResponseDto {
    private String applicationId;
    private String customerId;
    private String section;
    private String status; // SUCCESS, PENDING_VERIFICATION, FAILED
    private String workflowStatus; // STP_APPROVED, PENDING_BM_APPROVAL, PENDING_RPC_CHECKER
    private String nextRole;
    private List<String> validationErrors;
    private Map<String, Object> verificationDetails; // Penny drop response, OCR response, Fuzzy match score
}
