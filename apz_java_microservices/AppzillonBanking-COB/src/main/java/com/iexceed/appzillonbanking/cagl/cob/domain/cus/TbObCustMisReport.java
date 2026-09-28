package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Maps 1:1 to the actual tb_ob_cust_mis_report table as created by the DBA/CAGL IT team. Some
 * columns have no confirmed data source yet in this flow (t24CustomerId, applicationStatus,
 * kendraFrequency, loanFrequency, meetingDay, amount) - per the FSD, exact MIS parameters are
 * still to be finalized with the client, so those are left null for now rather than guessed at.
 *
 * One row per (applicationId, stageId, subStageId): the first submission for that combination
 * inserts it, every resubmission updates it in place - see {@link #modifiedCount}.
 */
@Entity
@Table(name = "tb_ob_cust_mis_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObCustMisReport {

    @EmbeddedId
    private MisReportId id;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "t24_customer_id")
    private String t24CustomerId;

    @Column(name = "appversion")
    private String appVersion;

    @Column(name = "applicationstatus")
    private String applicationStatus;

    @Column(name = "wfstatus")
    private String wfStatus;

    /** Flattened modifiedDetails the frontend sent, serialized as a JSON string (column is TEXT, not jsonb). */
    @Column(name = "editeddetails")
    private String editedDetails;

    @Column(name = "isedited")
    private String isEdited;

    @Column(name = "createdate")
    private String createDate;

    @Column(name = "updatedate")
    private String updateDate;

    @Column(name = "userrole")
    private String userRole;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "modifyby")
    private String modifyBy;

    @Column(name = "kendrafrequency")
    private String kendraFrequency;

    @Column(name = "loanfrequency")
    private String loanFrequency;

    @Column(name = "meetingday")
    private String meetingDay;

    @Column(name = "kendraid")
    private String kendraId;

    @Column(name = "groupid")
    private String groupId;

    @Column(name = "branchid")
    private String branchId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "addinfo2")
    private String addInfo2;

    @Column(name = "report_snapshot_ts", nullable = false)
    private LocalDateTime reportSnapshotTs;

    @Column(name = "record_type")
    private String recordType;

    @Column(name = "channel_type")
    private String channelType;

    /**
     * Overall per-submission modification counter (NOT per-field): 1 on first capture, then +1
     * every time this same (applicationId, stageId, subStageId) row is resubmitted with new
     * modifiedDetails - regardless of how many fields changed in that submission.
     *
     * NOTE: this column does not exist yet in tb_ob_cust_mis_report as it stands - it needs:
     *   ALTER TABLE tb_ob_cust_mis_report ADD COLUMN modified_count integer;
     * Until that migration runs, saving a row will fail the same way the earlier
     * modified_field_summary column did.
     */
    @Column(name = "modified_count")
    private Integer modifiedCount;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MisReportId implements Serializable {

        @Column(name = "applicationid", nullable = false)
        private String applicationId;

        @Column(name = "stageid", nullable = false)
        private String stageId;

        @Column(name = "substageid", nullable = false)
        private String subStageId;
    }
}
