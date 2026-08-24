package com.iexceed.appzillonbanking.cagl.loan.domain.ab;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_ucob_customer_audit_trail", schema = "public")
public class CustomerAuditTrailEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    @Column(name = "id")
    private Integer id;

    @JsonProperty("applicationId")
    @Column(name = "application_id")
    private String applicationId;

    @JsonProperty("userId")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("userRole")
    @Column(name = "user_role")
    private String userRole;

    @JsonProperty("stageId")
    @Column(name = "stage_id")
    private String stageId;

    @JsonProperty("createTs")
    @Column(name = "create_ts")
    private String createTs;

    @JsonProperty("type")
    @Column(name = "type")
    private String type;

    @JsonProperty("applicationStatus")
    @Column(name = "application_status")
    private String applicationStatus;

    @JsonProperty("customerId")
    @Column(name = "customer_id")
    private String customerId;

    @JsonProperty("customerName")
    @Column(name = "customer_name")
    private String customerName;

    @JsonProperty("kendraId")
    @Column(name = "kendraid")
    private String kendraId;

    @JsonProperty("kendraName")
    @Column(name = "kendra_name")
    private String kendraName;

    @JsonProperty("branchId")
    @Column(name = "branch_id")
    private String branchId;

    @JsonProperty("payload")
    @Column(name = "payload")
    private String payload;

    @JsonProperty("appVersion")
    @Column(name = "appversion")
    private String appVersion;

    @JsonProperty("addInfo1")
    @Column(name = "add_info1")
    private String addInfo1;

    @JsonProperty("addInfo2")
    @Column(name = "add_info2")
    private String addInfo2;

    @JsonProperty("remarks")
    @Column(name = "remarks")
    private String remarks;

}
