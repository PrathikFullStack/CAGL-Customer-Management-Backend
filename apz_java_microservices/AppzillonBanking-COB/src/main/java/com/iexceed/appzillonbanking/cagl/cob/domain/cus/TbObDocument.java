package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * tb_ob_document - composite PK (application_id, docu_id).
 * docu_id is a per-application sequence (1=Voter ID, 2=Aadhaar, 3=PAN ...),
 * hence @IdClass rather than a surrogate key.
 */
@Entity
@Table(name = "tb_ob_document")
@IdClass(TbObDocumentId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObDocument {

    @Id
    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Id
    @Column(name = "docu_id", length = 10, nullable = false)
    private String docuId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    /** memberKyc / FamilyMem / DeptentDoc / Address / Other */
    @Column(name = "category", length = 20, nullable = false)
    private String category;

    /** MemberDetails / Earning / Nominee / PerAddr / CommAddr / Other-1..3 */
    @Column(name = "sub_cat", length = 20, nullable = false)
    private String subCat;

    /** KYC / NONKYC / OCR */
    @Column(name = "kyc_type", length = 10, nullable = false)
    private String kycType;

    @Column(name = "auth_mode", length = 10)
    private String authMode;

    @Column(name = "id_type", length = 15)
    private String idType;

    @Column(name = "mem_relation", length = 30)
    private String memRelation;

    @Column(name = "mapping_id", length = 20)
    private String mappingId;

    @Column(name = "legal_doc_name", length = 30, nullable = false)
    private String legalDocName;

    @Column(name = "legal_doc_id", length = 50)
    private String legalDocId;

    @Column(name = "dms_doc_id_front", length = 20)
    private String dmsDocIdFront;

    @Column(name = "dms_doc_id_back", length = 20)
    private String dmsDocIdBack;

    @Column(name = "photo", length = 20)
    private String photo;

    @Column(name = "score", length = 20)
    private String score;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "add_info", columnDefinition = "TEXT")
    private String addInfo;

    //@Type(JsonType.class)
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "ocr_data", columnDefinition = "jsonb")
//    private Map<String, Object> ocrData;

    //@Type(JsonType.class)
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "input_data", columnDefinition = "jsonb")
//    private Map<String, Object> inputData;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited;

    @Column(name = "edited_by", length = 20)
    private String editedBy;

//    //@Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edited_fields", columnDefinition = "jsonb")
    private List<String> editedFields;

    @Column(name = "reuploaded_by", length = 20)
    private String reuploadedBy;

    //@Type(JsonType.class)
//    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reason")
    private String reason;

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;

    @Column(name = "clarity_pass", length = 1)
    private Character clarityPass;

    @Column(name = "dedupe_status", length = 10)
    private String dedupeStatus;

    @Column(name = "validation_status", length = 10)
    private String validationStatus;

    @Column(name = "doc_version", nullable = false)
    private Integer docVersion;

    @Column(name = "uploaded_by", length = 20, nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;
}
