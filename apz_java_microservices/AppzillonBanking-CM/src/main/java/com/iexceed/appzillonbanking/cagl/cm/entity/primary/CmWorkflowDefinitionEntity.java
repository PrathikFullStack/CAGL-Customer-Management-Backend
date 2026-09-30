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
@Table(name = "tb_ob_workflow_definition")
@IdClass(CmWorkflowDefinitionPK.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmWorkflowDefinitionEntity {

    @Id
    @Column(name = "app_id", length = 50, nullable = false)
    private String appId;

    @Id
    @Column(name = "workflow_id", length = 50, nullable = false)
    private String workflowId;

    @Column(name = "stage_seq_no", nullable = false)
    private Integer stageSeqNo;

    @Id
    @Column(name = "from_stage_id", length = 50, nullable = false)
    private String fromStageId;

    @Id
    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "next_stage_id", length = 50)
    private String nextStageId;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "rule_id", length = 50)
    private String ruleId;

    @Column(name = "curr_role", length = 50)
    private String currRole;

    @Column(name = "next_role", length = 50)
    private String nextRole;

    @Column(name = "next_workflow_status", length = 50)
    private String nextWorkflowStatus;

    @Column(name = "present_role", length = 255)
    private String presentRole;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;
}
