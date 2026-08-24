package com.iexceed.appzillonbanking.cagl.collection.domain.ab;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "TB_UACO_MAITRI_RETRY")
@IdClass(TbUacoMaitriRetryId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbUacoMaitriRetry {

    @Id
    private String appId;

    @Id
    private String customerId;

    @Id
    private String billNumber;

    @JsonProperty("uniqueIdentifier")
    @Column(name = "unique_identifier")
    private String uniqueIdentifier;

    @JsonProperty("maitriStatus")
    @Column(name = "maitri_status")
    private String maitriStatus;

    @JsonProperty("schedulerStatus")
    @Column(name = "scheduler_status")
    private String schedulerStatus;

    @JsonProperty("retryCount")
    @Column(name = "retry_count")
    private Integer retryCount;

    @JsonProperty("requestPayload")
    @Column(name = "request_payload")
    private String requestPayload;

    @JsonProperty("responsePayload")
    @Column(name = "response_payload")
    private String responsePayload;

    @JsonProperty("createTs")
    @Column(name = "create_ts")
    private Timestamp createTs;

    @JsonProperty("updateTs")
    @Column(name = "update_ts")
    private Timestamp updateTs;
}
