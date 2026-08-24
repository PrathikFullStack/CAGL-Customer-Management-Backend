package com.iexceed.appzillonbanking.cagl.cob.payload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupResponse {

    private String groupId;
    private String kendraId;
    private String status;
}
