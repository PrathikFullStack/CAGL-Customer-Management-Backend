package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.time.LocalDate;
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
@Table(name = "tb_cm_family_member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmFamilyMemberEntity {

    @Id
    @Column(name = "family_mem_id", length = 20, nullable = false)
    private String familyMemId;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "member_type", length = 3, nullable = false)
    private String memberType; // SP=Spouse, CO=Co-applicant, EM=Earning Member

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

    @Column(name = "is_nominee")
    private Boolean isNominee;

    @Column(name = "is_earning_member")
    private Boolean isEarningMember;

    @Column(name = "nominee_bank_details", columnDefinition = "jsonb")
    private String nomineeBankDetails;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;
}
