package com.iexceed.appzillonbanking.cagl.cm.payload.update;

import java.util.List;
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
public class UpdateResponseDto {
    private String applicationId;
    private String customerId;
    private String section;
    private String status;
    private String workflowStatus;
    private String nextRole;
    private String remarks;
    private List<String> validationErrors;
    private Map<String, Object> verificationDetails;
}
