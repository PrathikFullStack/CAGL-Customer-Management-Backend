package com.iexceed.appzillonbanking.cagl.incomeassesment.domain.ab;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_ucob_loan_report")
public class LoanReportEntity {

    @Id
    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "kendraid")
    private String kendraId;

    @Column(name = "branchid")
    private String branchId;

    @Column(name = "status")
    private String status;

    @Column(name = "loanaccnum")
    private String loanAccNum;

    @Column(name = "language")
    private String language;

    @Column(name = "commonpayload", columnDefinition = "TEXT")
    private String commonPayload;

    @Column(name = "dbpayload", columnDefinition = "TEXT")
    private String dbPayload;

    @Column(name = "passbookpayload", columnDefinition = "TEXT")
    private String passbookPayload;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "addinfo", columnDefinition = "TEXT")
    private String addInfo;

    @Column(name = "amlpayload", columnDefinition = "TEXT")
    private String amlPayload;
}