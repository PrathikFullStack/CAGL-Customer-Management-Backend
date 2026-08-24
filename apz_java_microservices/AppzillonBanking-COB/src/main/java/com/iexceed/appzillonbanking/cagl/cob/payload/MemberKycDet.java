package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Sub-stage 1.2 - primary member KYC documents (Voter ID / Aadhaar / PAN ...). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberKycDet {

    @JsonProperty("status")
    private String status;

    @JsonProperty("documentList")
    private List<DocumentListItem> documentList;
}
