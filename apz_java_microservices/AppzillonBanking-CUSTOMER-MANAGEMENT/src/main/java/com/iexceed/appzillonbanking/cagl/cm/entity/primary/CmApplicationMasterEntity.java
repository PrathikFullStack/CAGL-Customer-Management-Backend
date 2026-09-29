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
@Table(name = "tb_cm_application_master")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmApplicationMasterEntity {

    @Id
    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "version", length = 3, nullable = false)
    private String version;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "mobile_number", length = 12)
    private String mobileNumber;

    @Column(name = "branch_id", length = 20, nullable = false)
    private String branchId;

    @Column(name = "branch_name", length = 50, nullable = false)
    private String branchName;

    @Column(name = "kendra_id", length = 20)
    private String kendraId;

    @Column(name = "kendra_name", length = 100)
    private String kendraName;

    @Column(name = "group_id", length = 20)
    private String groupId;

    @Column(name = "km_name", length = 100, nullable = false)
    private String kmName;

    @Column(name = "leader", length = 20)
    private String leader;

    @Column(name = "stage", length = 30)
    private String stage;

    @Column(name = "sub_stage", length = 30)
    private String subStage;

    @Column(name = "wfstage", length = 30)
    private String wfstage;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "record_type", length = 15, nullable = false)
    private String recordType;

    @Column(name = "channel_type", length = 10)
    private String channelType;

    @Column(name = "loan_eligible", length = 50, nullable = false)
    private String loanEligible;

    @Column(name = "loan_id", length = 30)
    private String loanId;

    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
