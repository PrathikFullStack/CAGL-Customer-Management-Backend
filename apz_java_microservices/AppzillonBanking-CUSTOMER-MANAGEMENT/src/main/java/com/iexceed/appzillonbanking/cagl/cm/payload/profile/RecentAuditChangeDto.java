package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentAuditChangeDto {
    private String changeId;
    private String updatedBy;
    private String userRole;
    private String timestamp;
    private String stage;
    private String editedFieldsJson;
}
