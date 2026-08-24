package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.iexceed.appzillonbanking.cagl.cob.utils.StringSequenceGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** tb_ob_family_member - PK: family_mem_id (auto-increment). */
@Entity
@Table(name = "tb_ob_family_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObFamilyMember {

    @Id
    @StringSequenceGenerator(sequenceName = "seq_ob_family_mem_id")
    @Column(name = "family_mem_id", nullable = false)
    private String familyMemId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    /** SPOUSE / FATHER / EARNING / NOMINEE */
    @Column(name = "member_type", length = 15, nullable = false)
    private String memberType;

    @Column(name = "relation", length = 30, nullable = false)
    private String relation;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "mobile_num", length = 15)
    private String mobileNum;

    @Column(name = "kyc_type", length = 20)
    private String kycType;

    @Column(name = "kyc_doc_id", length = 50)
    private String kycDocId;

    @Column(name = "kyc_doc_front", length = 20)
    private String kycDocFront;

    @Column(name = "kyc_doc_back", length = 20)
    private String kycDocBack;

    @Column(name = "photo_doc_id", length = 20)
    private String photoDocId;

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;

    @Column(name = "cb_status", length = 15)
    private String cbStatus;

    @Column(name = "is_nominee", nullable = false)
    private Boolean isNominee;

    @Column(name = "is_earning_member", nullable = false)
    private Boolean isEarningMember;

    @Column(name = "is_edited_by", length = 20)
    private String isEditedBy;

//    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nominee_bank_details", columnDefinition = "jsonb")
    private Map<String, Object> nomineeBankDetails;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;
}
