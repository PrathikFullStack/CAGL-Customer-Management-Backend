package com.iexceed.appzillonbanking.cagl.cob.payload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateKendraResponse {

    private String kendraId;
    private String status;
}
