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
public class RequestWrapper<T> {
    private RequestHeader header;
    private T body;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestHeader {
        private String appId;
        private String userId;
        private String userRole;
        private String branchId;
        private String deviceId;
        private String screenId;
    }
}
