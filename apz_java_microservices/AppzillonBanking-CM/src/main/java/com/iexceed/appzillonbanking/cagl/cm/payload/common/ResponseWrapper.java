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
public class ResponseWrapper<T> {
    private ResponseHeader header;
    private T body;

    public static <T> ResponseWrapper<T> success(T body, String message) {
        return ResponseWrapper.<T>builder()
                .header(ResponseHeader.builder()
                        .status("SUCCESS")
                        .responseCode("200")
                        .responseMessage(message)
                        .build())
                .body(body)
                .build();
    }

    public static <T> ResponseWrapper<T> error(String code, String message) {
        return ResponseWrapper.<T>builder()
                .header(ResponseHeader.builder()
                        .status("FAILURE")
                        .responseCode(code)
                        .responseMessage(message)
                        .build())
                .body(null)
                .build();
    }
}
