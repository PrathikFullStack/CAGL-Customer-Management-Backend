package com.iexceed.appzillonbanking.cagl.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FetchMahiByBranchRequestWrapper {

    @JsonProperty("apiRequest")
    private FetchMahiByBranchRequest apiRequest;
}
