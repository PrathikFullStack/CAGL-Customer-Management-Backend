package com.iexceed.appzillonbanking.scheduler.domain.ab;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tb_ob_application_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObApplicationMaster {

    @Id
    @Column(name = "application_id")
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "customer_id")
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "version")
    @JsonProperty("version")
    private String version;

    @Column(name = "customer_name")
    @JsonProperty("customerName")
    private String customerName;

    @Column(name = "mobile_number")
    @JsonProperty("mobileNumber")
    private String mobileNumber;

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

    @Column(name = "branch_name")
    @JsonProperty("branchName")
    private String branchName;

    @Column(name = "km_name")
    @JsonProperty("kmName")
    private String kmName;

    @Column(name = "leader")
    @JsonProperty("leader")
    private String leader;

    @Column(name = "stage")
    @JsonProperty("stage")
    private String stage;

    @Column(name = "sub_stage")
    @JsonProperty("subStage")
    private String subStage;

    @Column(name = "wfstage")
    @JsonProperty("wfstage")
    private String wfStage;

    @Column(name = "status")
    @JsonProperty("status")
    private String status;

    @Column(name = "record_type")
    @JsonProperty("recordType")
    private String recordType;

    @Column(name = "channel_type")
    @JsonProperty("channelType")
    private String channelType;

    @Column(name = "source_stage")
    @JsonProperty("sourceStage")
    private String sourceStage;

    @Column(name = "dms_delete_flag")
    @JsonProperty("dmsDeleteFlag")
    private String dmsDeleteFlag;

    @Column(name = "dmsfolderidx")
    @JsonProperty("dmsFolderIdx")
    private String dmsFolderIdx;

    @Column(name = "dms_upload_flag")
    @JsonProperty("dmsUploadFlag")
    private String dmsUploadFlag;

    @Column(name = "loan_eligible")
    @JsonProperty("loanEligible")
    private String loanEligible;

    @Column(name = "loan_id")
    @JsonProperty("loanId")
    private String loanId;

    @Column(name = "lead_id")
    @JsonProperty("leadId")
    private String leadId;

    @Column(name = "cust_label")
    @JsonProperty("custLabel")
    private String custLabel;

    @Column(name = "created_by_role")
    @JsonProperty("createdByRole")
    private String createdByRole;

    @Column(name = "created_by")
    @JsonProperty("createdBy")
    private String createdBy;

    @Column(name = "created_ts")
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_by_role")
    @JsonProperty("updatedByRole")
    private String updatedByRole;

    @Column(name = "updated_by")
    @JsonProperty("updatedBy")
    private String updatedBy;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;

    @Column(name = "remarks")
    @JsonProperty("remarks")
    private String remarks;

    //    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "add_info1", columnDefinition = "jsonb")
    @Column(name = "add_info1", columnDefinition = "TEXT")
    @JsonProperty("addInfo1")
//    private Map<String, Object> addInfo1;
    private String addInfo1;

    //    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "add_info2", columnDefinition = "jsonb")
    @Column(name = "add_info2", columnDefinition = "TEXT")
    @JsonProperty("addInfo2")
//    private Map<String, Object> addInfo2;
    private String addInfo2;

    @Column(name = "photo_dedupe_status")
    @JsonProperty("photoDedupeStatus")
    @Builder.Default
    private String photoDedupeStatus="PENDING";

    /**
     * Advances the workflow only if the requested sub-stage is ahead
     * of the current sub-stage. Backward updates are ignored.
     */
    public void advanceSubStage(String requestedSubStage) {

        if (requestedSubStage == null || requestedSubStage.isBlank()) {
            return;
        }

        if (this.subStage == null || this.subStage.isBlank()) {
            this.subStage = requestedSubStage;
            return;
        }

        if (compareSubStages(requestedSubStage, this.subStage) > 0) {
            this.subStage = requestedSubStage;
        }
    }

    private int compareSubStages(String requested, String current) {
        String[] requestedParts = requested.split("\\.");
        String[] currentParts = current.split("\\.");

        int requestedStage = Integer.parseInt(requestedParts[0]);
        int currentStage = Integer.parseInt(currentParts[0]);

        if (requestedStage != currentStage) {
            return Integer.compare(requestedStage, currentStage);
        }

        int requestedSubStage = Integer.parseInt(requestedParts[1]);
        int currentSubStage = Integer.parseInt(currentParts[1]);

        return Integer.compare(requestedSubStage, currentSubStage);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class TbObApplicationMasterId implements Serializable {
        @Column(name = "application_id", length = 30)
        private String applicationId;

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "kendra_id")
        private Long kendraId;

        @Column(name = "group_id")
        private Long groupId;

        @Column(name = "lead_id", length = 30)
        private String leadId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class TbObAddressId implements Serializable {

        @Column(name = "address_id")
        private Long addressId;

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "application_id", length = 30)
        private String applicationId;
    }
}
