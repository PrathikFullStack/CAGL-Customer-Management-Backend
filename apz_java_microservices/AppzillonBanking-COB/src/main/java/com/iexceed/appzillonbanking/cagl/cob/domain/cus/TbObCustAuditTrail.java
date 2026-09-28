package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ob_cust_audit_trail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObCustAuditTrail {

    @Id
    @Column(name = "id")
    @JsonProperty("id")
    private String id;

    @Column(name = "app_id")
    @JsonProperty("appId")
    private String appId;

    @Column(name = "application_id", nullable = false)
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

    @Column(name = "stage_id")
    @JsonProperty("stageId")
    private String stageId;

    @Column(name = "sub_stage")
    @JsonProperty("subStage")
    private String subStage;

    @Column(name = "wfstatus")
    @JsonProperty("wfStatus")
    private String wfStatus;

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

    @Column(name = "product_id")
    @JsonProperty("productId")
    private String productId;

    @Column(name = "loan_amt")
    @JsonProperty("loanAmt")
    private String loanAmt;

    @Column(name = "purpose")
    @JsonProperty("purpose")
    private String purpose;

    @Column(name = "repayment_frequency")
    @JsonProperty("repaymentFrequency")
    private String repaymentFrequency;

    @Column(name = "payload")
    @JsonProperty("payload")
    private String payload;

    @Column(name = "editeddetails")
    @JsonProperty("editedDetails")
    private String editedDetails;

    @Column(name = "isedited")
    @JsonProperty("isEdited")
    private String isEdited;

    @Column(name = "add_info1")
    @JsonProperty("addInfo1")
    private String addInfo1;

    @Column(name = "add_info2")
    @JsonProperty("addInfo2")
    private String addInfo2;

    @Column(name = "add_info3")
    @JsonProperty("addInfo3")
    private String addInfo3;

    @Column(name = "add_info4")
    @JsonProperty("addInfo4")
    private String addInfo4;

    @Column(name = "app_version")
    @JsonProperty("appVersion")
    private String appVersion;

    @Column(name = "create_ts")
    @JsonProperty("createTs")
    private LocalDateTime createTs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class TbObLoanId implements Serializable {

        @Column(name = "loan_seq_id")
        private Long loanSeqId;

        @Column(name = "application_id", length = 30)
        private String applicationId;

        @Column(name = "customer_id")
        private Long customerId;
    }
}