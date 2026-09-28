package com.iexceed.appzillonbanking.cagl.cob.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckBlacklistResult {


    @JsonProperty("blacklisted")
    private boolean blacklisted;

    @JsonProperty("matchedOn")
    private String matchedOn;

    @JsonProperty("reason")
    private String reason;
}
