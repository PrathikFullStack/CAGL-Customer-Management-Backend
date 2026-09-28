package com.iexceed.appzillonbanking.cagl.cm.payload.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHeader {
    private String status;
    private String responseCode;
    private String responseMessage;
}
