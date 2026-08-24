package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_ob_photo_thumbnail")
@IdClass(TbObPhotoThumbnailId.class)
public class TbObPhotoThumbnail {

    // ── KEYS ──────────────────────────────────────────────
    @Id
    @JsonProperty("applicationId")
    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Id
    @JsonProperty("docuId")
    @Column(name = "docu_id", length = 20, nullable = false)
    private String docuId;
    
    @Id
    @JsonProperty("docuType")
    @Column(name = "docu_type", length = 20, nullable = false)
    private String docuType;

    @JsonProperty("customerId")
    @Column(name = "customer_id")
    private Long customerId;

    // ── IMAGE ─────────────────────────────────────────────
    @JsonProperty("mimeType")
    @Column(name = "mime_type", length = 20, nullable = false)
    @Builder.Default
    private String mimeType = "image/jpeg";

    @JsonProperty("width")
    @Column(name = "width", length = 5)
    private String width;

    @JsonProperty("height")
    @Column(name = "height", length = 5)
    private String height;

    @JsonProperty("fileSize")
    @Column(name = "file_size")
    private Integer fileSize;

    @JsonProperty("thumbnail")
    @Column(name = "thumbnail", nullable = false, columnDefinition = "bytea")
    private byte[] thumbnail;

    // ── AUDIT ─────────────────────────────────────────────
    @JsonProperty("createdTs")
    @CreationTimestamp
    @Column(name = "created_ts", nullable = false, updatable = false)
    private LocalDateTime createdTs;

    @JsonProperty("updatedTs")
    @UpdateTimestamp
    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
