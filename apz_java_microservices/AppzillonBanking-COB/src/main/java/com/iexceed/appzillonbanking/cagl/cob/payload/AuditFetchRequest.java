package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class AuditFetchRequest {

    @Valid
    @JsonProperty("requestObj")
    private AuditFetchRequestFields reqObj;
}
