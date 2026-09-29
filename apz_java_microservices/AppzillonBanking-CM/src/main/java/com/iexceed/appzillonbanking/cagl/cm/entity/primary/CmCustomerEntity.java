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
@Table(name = "tb_cm_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmCustomerEntity {

    @Id
    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "dob", length = 10)
    private String dob;

    @Column(name = "marital_status", length = 15)
    private String maritalStatus;

    @Column(name = "live_photo_status", length = 15)
    private String livePhotoStatus;

    @Column(name = "kyc_status", length = 15)
    private String kycStatus;

    @Column(name = "primary_kyc_type", length = 20)
    private String primaryKycType;

    @Column(name = "primary_kyc_id", length = 50)
    private String primaryKycId;

    @Column(name = "aml_status", length = 10)
    private String amlStatus;

    @Column(name = "bre_status", length = 15)
    private String breStatus;

    @Column(name = "cgt_status", length = 20)
    private String cgtStatus;

    @Column(name = "grt_status", length = 20)
    private String grtStatus;

    @Column(name = "kyc_details", columnDefinition = "jsonb")
    private String kycDetails;

    @Column(name = "bank_details", columnDefinition = "jsonb")
    private String bankDetails;

    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
