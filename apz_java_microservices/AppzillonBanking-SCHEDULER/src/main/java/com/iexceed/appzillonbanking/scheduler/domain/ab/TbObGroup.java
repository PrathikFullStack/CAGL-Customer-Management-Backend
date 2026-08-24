package com.iexceed.appzillonbanking.scheduler.domain.ab;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_ob_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TbObGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "kendra_id", nullable = false)
    private Long kendraId;

    @Column(name = "member_count", nullable = false)
    @Builder.Default
    private Integer memberCount = 0;

    @Column(name = "cgt_status", length = 15, nullable = false)
    @Builder.Default
    private String cgtStatus = "PENDING";

    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "dissolution_ts")
    private LocalDateTime dissolutionTs;

    @Column(name = "dissolution_reason", length = 200)
    private String dissolutionReason;

    @Column(name = "group_leader_id")
    private Long groupLeaderId;

    @Column(name = "kendra_leader_id")
    private Long kendraLeaderId;

    @Column(name = "t24_group_id", length = 30)
    private String t24GroupId;

    @Column(name = "last_activity_ts")
    private LocalDateTime lastActivityTs;

    @Column(name = "dms_folder_idx", length = 20)
    private String dmsFolderIdx;

    @Column(name = "photo_doc_id", length = 20)
    private String photoDocId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
