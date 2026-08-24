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
public class LeadCreationResult {

    private String leadId;
    private String memberName;
    private String mobileNumber;
    private String message;
    @JsonProperty("uId")
    private String uId;

}
