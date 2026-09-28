package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateDtls {

    /** Sent empty by the client; the server generates and returns it. */
    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("kendra_id")
    private String kendraId;

    @JsonProperty("kendra_name")
    private String kendraName;

    @JsonProperty("group_id")
    private String groupId;

    @NotBlank(message = "branch_id is required")
    @JsonProperty("branch_id")
    private String branchId;

    @NotBlank(message = "branch_name is required")
    @JsonProperty("branch_name")
    private String branchName;

    @NotBlank(message = "km_name is required")
    @JsonProperty("km_name")
    private String kmName;

    @JsonProperty("lead_id")
    private String leadId;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("customerDtls")
    private CustomerCreateDtls customerDtls;
}
