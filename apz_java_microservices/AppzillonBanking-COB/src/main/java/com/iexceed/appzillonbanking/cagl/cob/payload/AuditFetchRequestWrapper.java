package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.core.payload.Request;
import lombok.Data;

@Data
public class AuditFetchRequestWrapper extends Request {

    @JsonProperty("apiRequest")
    private AuditFetchRequest apiRequest;
}
