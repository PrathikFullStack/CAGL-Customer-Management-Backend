package com.iexceed.appzillonbanking.cagl.cm.payload.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHeader {
    private String status;
    private String responseCode;
    private String responseMessage;
}
