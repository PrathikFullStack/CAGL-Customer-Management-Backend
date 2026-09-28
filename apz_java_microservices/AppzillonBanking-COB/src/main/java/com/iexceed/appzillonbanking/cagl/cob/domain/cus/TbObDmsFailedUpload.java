package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tb_ob_dms_failed_upload",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_application_document",
                        columnNames = {"application_id", "document_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObDmsFailedUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_id")
    @JsonProperty("seqId")
    private Long seqId;

    @Column(name = "application_id", nullable = false)
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "document_id", nullable = false)
    @JsonProperty("documentId")
    private String documentId;

    @Column(name = "base_64")
    @JsonProperty("base64")
    private byte[] base64;

    @Column(name = "created_ts")
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;

    @Column(name = "payload", columnDefinition = "TEXT")
    @JsonProperty("payload")
    private String payload;

    @Column(name = "response", columnDefinition = "TEXT")
    @JsonProperty("response")
    private String response;

    @Column(name = "retry_count")
    @JsonProperty("retryCount")
    @Builder.Default
    private Integer retryCount = 0;
}