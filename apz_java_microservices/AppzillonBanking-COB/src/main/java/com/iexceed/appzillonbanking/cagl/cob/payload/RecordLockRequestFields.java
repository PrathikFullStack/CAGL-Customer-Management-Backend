package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordLockRequestFields {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("userId")
    private String userId;
}