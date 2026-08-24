package com.iexceed.appzillonbanking.cagl.cob.domain.ab;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ob__appln_workflow")
@IdClass(TbObApplnWorkflowId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObApplnWorkflow {

    @Id
    @Column(name = "app_id", nullable = false)
    private String appId;

    @Id
    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Id
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Id
    @Column(name = "workflow_seq_no", nullable = false)
    private Integer workflowSeqNo;

    @Column(name = "application_status")
    private String applicationStatus;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "present_role")
    private String presentRole;

    @Column(name = "next_workflow_stage")
    private String nextWorkflowStage;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_username", length = 500)
    private String createdUsername;
}
