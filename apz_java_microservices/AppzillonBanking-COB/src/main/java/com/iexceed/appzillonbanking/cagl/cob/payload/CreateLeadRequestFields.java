package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeadRequestFields {

    @JsonProperty("branchId")
    private String branchId;

    @JsonProperty("kendraId")
    private String kendraId;

    @JsonProperty("records")
    private List<LeadRecord> records;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LeadRecord {

        @NotBlank(message = "Member name is required")
        @JsonProperty("memberName")
        @Size(max = 100, message = "Member name must not exceed 100 characters")
        private String memberName;

        @NotBlank(message = "Member mobile number is required")
        @JsonProperty("memberMobile")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
        private String memberMobile;

        @JsonProperty("leadId")
        private String leadId;

        @JsonProperty("leadSource")
        private String leadSource;

        @JsonProperty("villageAreaName")
        private String villageAreaName;

        @JsonProperty("referredBy")
        private String referredBy;

        @JsonProperty("referenceMobile")
        private String referenceMobile;

        @JsonProperty("kycType")
        private String kycType;

        @JsonProperty("kycId")
        private String kycId;

        @JsonProperty("landmark")
        private String landmark;

        @JsonProperty("selectedForOnboarding")
        private boolean selectedForOnboarding;
    }
}
