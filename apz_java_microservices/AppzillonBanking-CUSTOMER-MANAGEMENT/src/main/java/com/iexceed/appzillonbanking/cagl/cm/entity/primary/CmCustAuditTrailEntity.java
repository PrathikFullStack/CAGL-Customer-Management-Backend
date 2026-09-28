package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_cm_cust_audit_trail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmCustAuditTrailEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "customer_id", length = 20)
    private String customerId;

    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "stage_id", length = 20)
    private String stageId;

    @Column(name = "sub_stage", length = 20)
    private String subStage;

    @Column(name = "wfstatus", length = 20)
    private String wfstatus;

    @Column(name = "isedited", length = 10)
    private String isedited;

    @Column(name = "editeddetails", columnDefinition = "jsonb")
    private String editeddetails;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "create_ts")
    private LocalDateTime createTs;
}
