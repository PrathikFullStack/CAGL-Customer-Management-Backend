package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Sub-stage 1.3 - permanent (PA) and communication (CA) address details. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalAddressDet {

    @JsonProperty("nameSelected")
    private String nameSelected;

    @JsonProperty("dobSelected")
    private String dobSelected;

    @JsonProperty("PA")
    private String pa;

    @JsonProperty("CA")
    private String ca;

    @JsonProperty("status")
    private String status;

    @JsonProperty("otherDetails")
    private Map<String, Object> otherDetails;

    /** Note: unlike memberKycDetails, these entries are NOT wrapped in an extra "documentDetails" envelope. */
    @JsonProperty("documentList")
    private List<DocumentListItem> documentList;
}
