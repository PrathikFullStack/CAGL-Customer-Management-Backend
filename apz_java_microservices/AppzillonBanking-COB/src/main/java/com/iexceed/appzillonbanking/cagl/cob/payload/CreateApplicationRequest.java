package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateApplicationRequest {

    @NotBlank(message = "interfaceName is required")
    @JsonProperty("interfaceName")
    private String interfaceName;

    @NotBlank(message = "appId is required")
    @JsonProperty("appId")
    private String appId;

    @NotBlank(message = "userId is required")
    @JsonProperty("userId")
    private String userId;

    @NotBlank(message = "userRole is required")
    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("appVersion")
    private String appVersion;

    @JsonProperty("userName")
    private String userName;

    @NotBlank(message = "branchId is required")
    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("remarks")
    private String remarks;

    @NotNull(message = "requestObj is required")
    @Valid
    @JsonProperty("requestObj")
    private CreateApplicationRequestFields requestObj;
}
