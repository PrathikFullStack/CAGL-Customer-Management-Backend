package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_ob_lead")
public class TbObLead {

    @Id
    @Column(name = "lead_id", nullable = false, updatable = false)
    private String leadId;

    @JsonProperty("uId")
    @Column(name = "u_id", length = 20)
    private String uId;

    @JsonProperty("leadSource")
    @Column(name = "lead_source", length = 20, nullable = false)
    private String leadSource;

    @JsonProperty("mobileNumber")
    @Column(name = "mobile_number", length = 15, nullable = false)
    private String mobileNumber;

    @JsonProperty("customerName")
    @Column(name = "customer_name", length = 100)
    private String customerName;

    @JsonProperty("branchId")
    @Column(name = "branch_id", length = 20, nullable = false)
    private String branchId;

    @JsonProperty("kmId")
    @Column(name = "km_id", length = 20, nullable = false)
    private String kmId;

    @JsonProperty("status")
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private String status = "OPEN";

    @JsonProperty("applicationId")
    @Column(name = "application_id", length = 30, unique = true)
    private String applicationId;

    @JsonProperty("convertedTs")
    @Column(name = "converted_ts")
    private LocalDateTime convertedTs;

    @JsonProperty("remarks")
    @Column(name = "remarks", length = 500)
    private String remarks;

    @JsonProperty("addInfo")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "add_info", columnDefinition = "jsonb")
    private Map<String, Object> addInfo;

    @JsonProperty("createdBy")
    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @JsonProperty("createdTs")
    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @JsonProperty("updatedBy")
    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @JsonProperty("updatedTs")
    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
