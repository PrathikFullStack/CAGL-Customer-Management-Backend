package com.iexceed.appzillonbanking.cagl.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "gk_unified_lead_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GkUnifiedLeadData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "leadId")
    private String leadId;

    @Column(name = "leadName")
    private String leadName;

    @Column(name = "leadType", nullable = false)
    private String leadType;

    @Column(name = "leadSource")
    private String leadSource;

    @Column(name = "applicationId", unique = true)
    private String applicationId;

    @Column(name = "customerId")
    private String customerId;

    @Column(name = "customerName")
    private String customerName;

    @Column(name = "branchId")
    private String branchId;

    @Column(name = "branchName")
    private String branchName;

    @Column(name = "kendraId")
    private String kendraId;

    @Column(name = "kendraName")
    private String kendraName;

    @Column(name = "primaryId", columnDefinition = "text")
    private String primaryId;

    @Column(name = "primaryType", columnDefinition = "text")
    private String primaryType;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "eligibleAmount")
    private String eligibleAmount;

    @Column(name = "zone")
    private String zone;

    @Column(name = "dropOutDate")
    private String dropOutDate;

    @Column(name = "leadMobileNumber")
    private String leadMobileNumber;

    @Column(name = "referringCustomerId")
    private String referringCustomerId;

    @Column(name = "referringCustomerName")
    private String referringCustomerName;

    @Column(name = "referringCustomerMobileNumber")
    private String referringCustomerMobileNumber;

    @Column(name = "referringCustomerKendraId")
    private String referringCustomerKendraId;

    @Column(name = "referringCustomerBranchId")
    private String referringCustomerBranchId;

    @Column(name = "referredCustomerMobileNumber")
    private String referredCustomerMobileNumber;

    @Column(name = "referredCustomerName")
    private String referredCustomerName;

    @Column(name = "userId")
    private String userId;

    @Column(name = "kmId")
    private String kmId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addInfo", columnDefinition = "json")
    private Map<String, Object> addInfo;

    @Column(name = "convertedTs")
    private LocalDateTime convertedTs;

    @Column(name = "leadStatus")
    private String leadStatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "updatedTs", insertable = false, updatable = false)
    private LocalDateTime updatedTs;
}
