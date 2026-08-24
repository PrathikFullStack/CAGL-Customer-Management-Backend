package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class AuditTrailRecord {
        @JsonProperty("auditSource")
        private String auditSource; // "APPLICATION" or "USER"

        @JsonProperty("eventType")
        private String eventType;

        @JsonProperty("eventTimestamp")
        private LocalDateTime eventTimestamp;

        @JsonProperty("userId")
        private String userId;

        @JsonProperty("userName")
        private String userName;

        @JsonProperty("userRole")
        private String userRole;

        @JsonProperty("details")
        private String details;
    }

