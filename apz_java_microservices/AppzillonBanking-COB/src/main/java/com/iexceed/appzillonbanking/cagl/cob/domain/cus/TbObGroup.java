package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ob_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "kendra_id", nullable = false)
    private String kendraId;

    @Column(name = "cgt_status", nullable = false)
    @Builder.Default
    private String cgtStatus = "PENDING";

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "dissolution_ts")
    private LocalDateTime dissolutionTs;

    @Column(name = "dissolution_reason")
    private String dissolutionReason;

    @Column(name = "group_leader_id")
    private String groupLeaderId;

    @Column(name = "t24_group_id")
    private String t24GroupId;

    @Column(name = "total_member_count", nullable = false)
    private String totalMemberCount;

    @Column(name = "activated_count", nullable = false)
    private String activatedCount;

    @Column(name = "inactive_count", nullable = false)
    private String inactiveCount;

    @Column(name = "inprogress_count", nullable = false)
    private String inprogressCount;

    @Column(name = "released_count", nullable = false)
    private String releasedCount;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

    @Column(name = "dms_folder_idx")
    private String dmsFolderIdx;

    @Column(name = "photo_doc_id")
    private String photoDocId;

    @Column(name = "payload")
    private String payload;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "stage", nullable = false)
    @Builder.Default
    private String stage = "NA";

}