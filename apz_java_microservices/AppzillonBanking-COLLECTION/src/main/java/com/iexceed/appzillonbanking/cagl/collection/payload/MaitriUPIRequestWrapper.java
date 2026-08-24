package com.iexceed.appzillonbanking.cagl.collection.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MaitriUPIRequestWrapper {

    @JsonProperty("apiRequest")
    private MaitriUPIRequest apiRequest;
}
