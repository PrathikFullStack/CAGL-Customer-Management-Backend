package com.iexceed.appzillonbanking.cagl.collection.domain.ab;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name ="tb_uaco_interim_application_master")
@IdClass(TbUacoInterimApplicationMasterId.class)
@NoArgsConstructor
@AllArgsConstructor
public class TbUacoInterimApplicationMaster {

    @Id
    private String appId;

    @Id
    private String applicationId;

    @Id
    private int latestVersionNo;

    @Id
    private String kendraId;

    @JsonProperty("applicationDate")
    @Column(name = "application_date")
    private LocalDate applicationDate;

    @JsonProperty("createTs")
    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "application_type")
    private String applicationType;

    @Column(name = "application_status")
    private String applicationStatus;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "current_stage")
    private String currentStage;

    @Column(name = "kmid")
    private String kmid;

    @Column(name = "kendraname")
    private String kendraname;

    @Column(name = "add_info")
    private String addInfo;

    @Column(name = "payload")
    private String payload;
}
