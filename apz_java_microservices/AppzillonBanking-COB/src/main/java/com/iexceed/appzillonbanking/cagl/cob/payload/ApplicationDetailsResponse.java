package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObAddress;
import lombok.Builder;
import java.util.List;

@Builder
public record ApplicationDetailsResponse(
        String applicationId,
        String kendraId,
        String groupId,
        String branchId,
        String customerId,
        String stage,
        String subStage,
        String wfStage,
        CustomerDetailsDto customerDetails,
        BreCheckDto breCheck,
//        List<WorkflowHistoryDto> workflowHistory,
//        List<AuditEventDto> auditSnapshot,
//        LockInfoDto lockInfo,
        AdditionalApplicationData applicationData,
        List<AddressDetailsDto> addresses,
        BMReInterviewDetailsDto bmReInterviewDetails
) {
    // Canonical constructor enforces immutability + null-safety for collections,
    // regardless of what the builder or caller passes in.
    public ApplicationDetailsResponse {
        addresses = addresses == null ? List.of() : List.copyOf(addresses);
    }
}