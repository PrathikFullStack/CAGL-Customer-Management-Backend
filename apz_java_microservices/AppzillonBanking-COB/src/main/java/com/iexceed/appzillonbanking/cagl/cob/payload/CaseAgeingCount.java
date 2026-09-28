package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseAgeingCount {

    // Echo this value back as requestObj.ageing to filter the list to this bucket.
    @JsonProperty("days")
    private int days;

    @JsonProperty("count")
    private long count;
}
