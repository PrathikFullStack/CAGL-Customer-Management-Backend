package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateApplicationRequestFields {

    @Valid
    @NotEmpty(message = "applicationdtls must not be empty")
    @JsonProperty("applicationdtls")
    private List<ApplicationCreateDtls> applicationdtls;

    @JsonProperty("requestType")
    private String requestType;
}
