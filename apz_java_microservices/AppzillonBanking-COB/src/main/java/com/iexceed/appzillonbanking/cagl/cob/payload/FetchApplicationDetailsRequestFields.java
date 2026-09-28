package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchApplicationDetailsRequestFields {
    private String applicationId;
    private String userId;
    private String userName;
    private String userRole;
    private String accessType = AccessEnum.RESTRICT.name();

    private enum AccessEnum {
        VIEW,
        RESTRICT
    }
}