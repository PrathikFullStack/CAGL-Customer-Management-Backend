package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class AuditTrailRecord {
        @JsonProperty("auditSource")
        private String auditSource; // "APPLICATION" or "USER"
        @JsonProperty("eventType")
        private String eventType;
        @JsonProperty("id")
        private String id;

        @JsonProperty("eventTimestamp")
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss.SSS")
        private LocalDateTime eventTimestamp;

        @JsonProperty("userId")
        private String userId;

        @JsonProperty("userName")
        private String userName;

        @JsonProperty("userRole")
        private String userRole;

        @JsonProperty("details")
        private String details;

        @JsonProperty("editedDetails")
        private Map<String, Object> editedDetails;

        @JsonProperty("payload")
        private Map<String, Object> payload;

        @JsonProperty("isEdited")
        private Boolean isEdited;
    }
