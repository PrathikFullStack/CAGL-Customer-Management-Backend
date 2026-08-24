package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ob_other_document")
@Data
@NoArgsConstructor
@IdClass(TbObOtherDocumentPK.class)
@AllArgsConstructor
@Builder
public class TbObOtherDocument{

	@Id
	@Column(name = "application_id", nullable = false)
	private String applicationId;

    @Id
    @Column(name = "docu_id", nullable = false)
    private String docuId;

//    @Id
//    @Column(name = "id")
//    private String id;

    @Id
    @Column(name = "category", nullable = false)
    private String category;

	@Column(name = "sub_cat", length = 20, nullable = false)
	private String subCat;

	@Column(name = "kyc_type", length = 10, nullable = false)
	private String kycType;

	@Column(name = "auth_mode", length = 10)
	private String authMode;

	@Column(name = "id_type", length = 15)
	private String idType;

	@Column(name = "mem_relation", length = 30)
	private String memRelation;

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

//	@Column(name = "ocr_data", columnDefinition = "jsonb")
//	@Lob
//	private String ocrData;

//	@Column(name = "input_data", columnDefinition = "jsonb")
//	@Lob
//	private String inputData;

	@Column(name = "is_edited", nullable = false)
	@Builder.Default
	private Boolean isEdited = false;

	@Column(name = "edited_by", length = 20)
	private String editedBy;

	@Column(name = "edited_fields", columnDefinition = "jsonb")
	@Lob
	private List<String> editedFields;

	@Column(name = "reuploaded_by", length = 20)
	private String reuploadedBy;

	@Column(name = "reason", columnDefinition = "jsonb")
	@Lob
	private String reason;

	@Column(name = "clarity_score", precision = 5)
	private BigDecimal clarityScore;

	@Column(name = "clarity_pass", length = 1)
	private String clarityPass;

	@Column(name = "dedupe_status", length = 10)
	private String dedupeStatus;

	@Column(name = "validation_status", length = 10)
	private String validationStatus;

	@Column(name = "doc_version", nullable = false)
	@Builder.Default
	private Integer docVersion = 1;

	@Column(name = "uploaded_by", length = 20, nullable = false)
	private String uploadedBy;

	@Column(name = "uploaded_at", nullable = false)
	private Long uploadedAt;

	@Column(name = "created_ts", nullable = false)
	private Long createdTs;

	@Column(name = "updated_ts")
	private Long updatedTs;

}