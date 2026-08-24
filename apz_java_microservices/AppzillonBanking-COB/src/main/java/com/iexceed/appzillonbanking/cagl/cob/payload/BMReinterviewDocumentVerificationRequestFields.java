package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BMReinterviewDocumentVerificationRequestFields {

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("documentName")
    private String documentName;

    @JsonProperty("verified") // Overall doc_verification flag (BM must verify member + spouse + earning member docs.)
    private Boolean verified;

    @JsonProperty("verifiedTs") // there is a checkbox  called "originally seen & verify"
    private Long verifiedTs;
}
