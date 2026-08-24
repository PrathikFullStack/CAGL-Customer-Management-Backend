package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Sub-stage 1.7 - bank account details for disbursement. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDet {

    @JsonProperty("bankAccNo")
    private String bankAccNo;

    @JsonProperty("bankAccName")
    private String bankAccName;

    @JsonProperty("bankBranchName")
    private String bankBranchName;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("bankIfscCode")
    private String bankIfscCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pennyRes")
    private Map<String, Object> pennyRes;

    @JsonProperty("documentList")
    private List<DocumentListItem> documentList;
}
