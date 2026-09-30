package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cm_appln_workflow")
@IdClass(CmApplnWorkflowPK.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmApplnWorkflowEntity {

    @Id
    @Column(name = "app_id", length = 255, nullable = false)
    private String appId;

    @Id
    @Column(name = "application_id", length = 255, nullable = false)
    private String applicationId;

    @Id
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Id
    @Column(name = "workflow_seq_no", nullable = false)
    private Integer workflowSeqNo;

    @Column(name = "application_status", length = 255)
    private String applicationStatus;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "present_role", length = 255)
    private String presentRole;

    @Column(name = "next_workflow_stage", length = 255)
    private String nextWorkflowStage;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_username", length = 500)
    private String createdUsername;
}
