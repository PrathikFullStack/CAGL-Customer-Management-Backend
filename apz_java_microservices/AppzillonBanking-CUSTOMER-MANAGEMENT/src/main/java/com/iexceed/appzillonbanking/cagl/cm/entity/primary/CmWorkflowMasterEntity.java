package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ob_workflow_master")
@IdClass(CmWorkflowMasterPK.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmWorkflowMasterEntity {

    @Id
    @Column(name = "app_id", length = 20, nullable = false)
    private String appId;

    @Id
    @Column(name = "workflow_id", length = 30, nullable = false)
    private String workflowId;

    @Column(name = "workflow_desc", length = 200)
    private String workflowDesc;

    @Column(name = "workflow_status", length = 20)
    private String workflowStatus;
}
