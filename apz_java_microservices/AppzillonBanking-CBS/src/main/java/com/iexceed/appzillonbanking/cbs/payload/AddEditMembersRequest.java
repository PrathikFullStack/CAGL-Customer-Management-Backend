package com.iexceed.appzillonbanking.cbs.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEditMembersRequest {
    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("versionNum")
    private String versionNum;

    @JsonProperty("requestObj")
    private List<AddEditMembersRequestFields> requestObj;
}
