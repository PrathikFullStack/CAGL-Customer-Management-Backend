package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.VerificationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Superset of every field the client can send under "customerDtls" across
 * sub-stages 1.2 - 1.8. Only the section(s) relevant to the sub_stage being
 * submitted will be populated on any given request; the service layer reads
 * only the block matching application_id's current sub_stage.
 *
 * kycDetails/payload vary in shape stage to stage (new keys are added as the
 * journey progresses) so they are kept as a flexible map rather than a rigid
 * class - everything else that maps 1:1 to a DB table/column is strongly typed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateDtls {

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("photo_doc_id")
    private String photoDocId;

    @JsonProperty("kycDetails")
    private Map<String, Object> kycDetails;

    @JsonProperty("payload")
    private Map<String, Object> payload;

    @JsonProperty("memberPhoto")
    private MemberPhotoDet memberPhoto;

    @JsonProperty("memberKycDetails")
    private MemberKycDet memberKycDetails;

    @JsonProperty("personalAddressDet")
    private PersonalAddressDet personalAddressDet;

    @JsonProperty("familyDetails")
    private FamilyDet familyDetails;

    @JsonProperty("incomeDet")
    private IncomeDet incomeDet;

    @JsonProperty("kendraSelectionDetails")
    private KendraSelectionDetails kendraSelectionDetails;

    @JsonProperty("bankDet")
    private BankDet bankDet;

    @JsonProperty("additionalDocuDet")
    private AdditionalDocuDet additionalDocuDet;

    @JsonProperty("verficationDet")
    private Map<String, Object> verficationDet;
}
