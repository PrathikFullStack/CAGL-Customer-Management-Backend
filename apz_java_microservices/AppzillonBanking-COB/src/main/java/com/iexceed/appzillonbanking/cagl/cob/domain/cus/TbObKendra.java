package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ob_kendra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObKendra implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kendra_id", nullable = false)
    private String kendraId;

    @Column(name = "kendra_name", nullable = false)
    private String kendraName;

    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Column(name = "km_id")
    private String kmId;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "state")
    private Integer state;

    @Column(name = "district")
    private String district;

    @Column(name = "village")
    private String village;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "gps_latitude")
    private BigDecimal gpsLatitude;

    @Column(name = "gps_longitude")
    private BigDecimal gpsLongitude;

    @Column(name = "distance_from_branch")
    private BigDecimal distanceFromBranch;

    @Column(name = "meeting_day")
    private String meetingDay;

    @Column(name = "meeting_time")
    private String meetingTime;

    @Column(name = "meeting_place")
    private String meetingPlace;

    @Column(name = "meeting_frequency")
    private String meetingFrequency;

    @Column(name = "first_meeting_date")
    private LocalDate firstMeetingDate;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "kendra_leader_id")
    private String kendraLeaderId;

    @Column(name = "blacklist_status", nullable = false)
    @Builder.Default
    private String blacklistStatus = "PENDING";

    @Column(name = "blacklist_ts")
    private LocalDateTime blacklistTs;

    @Column(name = "t24_kendra_id")
    private String t24KendraId;

    @Column(name = "last_activity_ts")
    private LocalDateTime lastActivityTs;

    @Column(name = "dms_folder_idx")
    private String dmsFolderIdx;

    @Column(name = "photo_doc_id")
    private String photoDocId;

    @Column(name = "payload")
    private String payload;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

    @Column(name="total_group_count")
    private Integer totalGroupCount;


}