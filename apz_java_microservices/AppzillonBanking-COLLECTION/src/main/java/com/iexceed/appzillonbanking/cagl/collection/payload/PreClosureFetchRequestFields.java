package com.iexceed.appzillonbanking.cagl.collection.payload;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreClosureFetchRequestFields {

    @JsonProperty("arrangementIdStr")
    private String arrangementIdStr;

}
