package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMemberItem {

    @JsonProperty("MemberType")
    private String memberType;

    @JsonProperty("RelationType")
    private String relationType;

    @JsonProperty("isEarning")
    private Boolean isEarning;

    @JsonProperty("isNominee")
    private Boolean isNominee;

    @JsonProperty("mobileNum")
    private String mobileNum;

    @JsonProperty("documentList")
    private List<DocumentListItem> documentList;
}
