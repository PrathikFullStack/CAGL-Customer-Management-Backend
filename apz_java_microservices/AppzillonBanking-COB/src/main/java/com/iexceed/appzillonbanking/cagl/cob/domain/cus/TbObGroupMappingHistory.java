package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ob_group_mapping_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObGroupMappingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private String mappingId;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "from_group_id", nullable = false)
    private String fromGroupId;

    @Column(name = "from_kendra_id", nullable = false)
    private String fromKendraId;

    @Column(name = "to_group_id", nullable = false)
    private String toGroupId;

    @Column(name = "to_kendra_id", nullable = false)
    private String toKendraId;

    @Column(name = "customer_current_status")
    private String customerCurrentStatus;

    @Column(name = "mapping_type")
    private String mappingType;

    @Column(name = "pre_member_count")
    private String preMemberCount;

    @Column(name = "current_member_count")
    private String currentMemberCount;

    @Column(name = "related_group_id")
    private String relatedGroupId;

    @Column(name = "affected_customer_id")
    private String affectedCustomerId;

    @Column(name = "mapping_reason")
    private String mappingReason;

    @Column(name = "mapping_date")
    private LocalDate mappingDate;

    @Column(name = "dms_folder_idx")
    private String dmsFolderIdx;

    @Column(name = "photo_doc_id")
    private String photoDocId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;
}