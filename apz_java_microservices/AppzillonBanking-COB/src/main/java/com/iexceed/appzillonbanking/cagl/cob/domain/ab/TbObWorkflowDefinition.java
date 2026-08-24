package com.iexceed.appzillonbanking.cagl.cob.domain.ab;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ob_workflow_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TbObWorkflowDefinition.TbObWorkflowDefinitionId.class)
public class TbObWorkflowDefinition {

    @Id
    @Column(name = "app_id", nullable = false)
    private String appId;

    @Id
    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @Id
    @Column(name = "stage_seq_no", nullable = false)
    private Integer stageSeqNo;

    @Column(name = "from_stage_id", nullable = false)
    private String fromStageId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "next_stage_id", nullable = false)
    private String nextStageId;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "rule_id")
    private String ruleId;

    @Column(name = "curr_role")
    private String currRole;

    @Column(name = "next_role")
    private String nextRole;

    @Column(name = "next_workflow_status")
    private String nextWorkflowStatus;

    @Column(name = "present_role", length = 255)
    private String presentRole;

    @Column(name = "remarks")
    private String remarks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class TbObWorkflowDefinitionId implements Serializable {
        private String appId;
        private String workflowId;
        private Integer stageSeqNo;
    }
}