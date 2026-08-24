package com.iexceed.appzillonbanking.cagl.collection.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreclosureDedupeRequestFields {

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("meetingDate")
    private String meetingDate;

}
