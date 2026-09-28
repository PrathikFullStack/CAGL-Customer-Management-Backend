// IdDescPair.java — repayFrequency aur disburseMode for both reusable
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
public class IdDescPair {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("idDesc")
    private String idDesc;
}