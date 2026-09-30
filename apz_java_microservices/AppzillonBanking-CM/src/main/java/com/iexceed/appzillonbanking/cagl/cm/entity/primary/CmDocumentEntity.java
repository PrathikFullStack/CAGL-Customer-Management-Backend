package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cm_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmDocumentEntity {

    @Id
    @Column(name = "docu_id", length = 20, nullable = false)
    private String docuId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "category", length = 20, nullable = false)
    private String category;

    @Column(name = "sub_cat", length = 20, nullable = false)
    private String subCat;

    @Column(name = "kyc_type", length = 10, nullable = false)
    private String kycType;

    @Column(name = "legal_doc_name", length = 30, nullable = false)
    private String legalDocName;

    @Column(name = "legal_doc_id", length = 50)
    private String legalDocId;

    @Column(name = "dms_doc_id_front", length = 50)
    private String dmsDocIdFront;

    @Column(name = "dms_doc_id_back", length = 50)
    private String dmsDocIdBack;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;

    @Column(name = "clarity_pass", length = 1)
    private String clarityPass;

    @Column(name = "dedupe_status", length = 10)
    private String dedupeStatus;

    @Column(name = "validation_status", length = 10)
    private String validationStatus;

    @Column(name = "doc_version", nullable = false)
    private Integer docVersion;

    @Column(name = "uploaded_by", length = 20, nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
