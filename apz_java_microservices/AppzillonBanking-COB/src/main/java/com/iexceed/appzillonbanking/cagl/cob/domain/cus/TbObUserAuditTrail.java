package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_ob_user_audit_trail")
public class TbObUserAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "app_id")
    @JsonProperty("appId")
    private String appId;

    @Column(name = "application_id")
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "user_id")
    @JsonProperty("userId")
    private String userId;

    @Column(name = "user_name")
    @JsonProperty("userName")
    private String userName;

    @Column(name = "user_role")
    @JsonProperty("userRole")
    private String userRole;

    @Column(name = "service_type")
    @JsonProperty("serviceType")
    private String serviceType;

    @Column(name = "sub_stage")
    @JsonProperty("subStage")
    private String subStage;

    @Column(name = "customer_id")
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "customer_name")
    @JsonProperty("customerName")
    private String customerName;

    @Column(name = "mobile_no")
    @JsonProperty("mobileNo")
    private String mobileNo;

    @Column(name = "kendra_id")
    @JsonProperty("kendraId")
    private String kendraId;

    @Column(name = "kendra_name")
    @JsonProperty("kendraName")
    private String kendraName;

    @Column(name = "group_id")
    @JsonProperty("groupId")
    private String groupId;

    @Column(name = "branch_id")
    @JsonProperty("branchId")
    private String branchId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    @JsonProperty("payload")
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "add_info1", columnDefinition = "jsonb")
    @JsonProperty("addInfo1")
    private Map<String, Object> addInfo1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "add_info2", columnDefinition = "jsonb")
    @JsonProperty("addInfo2")
    private Map<String, Object> addInfo2;

    @Column(name = "app_version")
    @JsonProperty("appVersion")
    private String appVersion;

    @CreationTimestamp
    @Column(name = "create_ts", updatable = false)
    @JsonProperty("createTs")
    private LocalDateTime createTs;
}
