package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tb_ob_cgt_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObCGTDetails {

    @Id
    @JsonProperty("cgtId")
    @Column(name = "cgt_id", nullable = false)
    private String cgtId;

    @JsonProperty("groupId")
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @JsonProperty("kendraId")
    @Column(name = "kendra_id", nullable = false)
    private String kendraId;

    @JsonProperty("addCGTPayload")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "add_cgt_payload", columnDefinition = "jsonb")
    private List<Object> addCGTPayload;

    @JsonProperty("conductCGTPayload")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conduct_cgt_payload", columnDefinition = "jsonb")
    private List<Object> conductCGTPayload;

    @JsonProperty("status")
    @Column(name = "status")
    private String status;

    @JsonProperty("subStage")
    @Column(name = "sub_stage")
    private String subStage;

    @JsonProperty("endCgtTs")
    @Column(name = "end_cgt_ts")
    private LocalDateTime endCgtTs;

    @JsonProperty("groupPhotoId")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "group_photo_id", columnDefinition = "jsonb")
    @Column(name = "group_photo_id")
//    private Map<String, Object> groupPhotoId;
    private String groupPhotoId;

    @JsonProperty("annexureId")
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "annexure_id", columnDefinition = "jsonb")
    @Column(name = "annexure_id")
//    private Map<String, Object> annexureId;
    private String annexureId;

    @JsonProperty("createdBy")
    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_ts", updatable = false)
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @JsonProperty("updatedBy")
    @Column(name = "updated_by")
    private String updatedBy;

    @JsonProperty("updatedTs")
    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

}

