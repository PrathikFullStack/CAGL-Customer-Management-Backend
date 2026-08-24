package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Some lists wrap each entry in an extra "documentDetails" envelope
 * (memberKycDetails.documentList[], familyMember.documentList[]) - this class
 * models that outer wrapper. Where the client sends a bare list of
 * DocumentDetail directly (personalAddressDet, incomDet, bankDet,
 * AdditionalDocuDet) use List&lt;DocumentDetail&gt; instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentListItem {

    @JsonProperty("documentDetails")
    private DocumentDetail documentDetails;
}
