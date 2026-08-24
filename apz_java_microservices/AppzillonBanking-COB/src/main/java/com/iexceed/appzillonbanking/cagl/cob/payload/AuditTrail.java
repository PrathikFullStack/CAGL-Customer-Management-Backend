package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditTrail {
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("eventDescription")
    private String eventDescription;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("userRole")
    private String userRole;
    @JsonProperty("details")
    private String details;
}