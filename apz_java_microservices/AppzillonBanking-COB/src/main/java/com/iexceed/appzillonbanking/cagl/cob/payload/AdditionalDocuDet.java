package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Sub-stage 1.8 - home / business photos and any other supporting docs. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalDocuDet {

    @JsonProperty("documentList")
    private List<DocumentListItem> documentList;
}
