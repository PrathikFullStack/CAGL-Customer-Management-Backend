package com.iexceed.appzillonbanking.cagl.incomeassesment.domain.ab;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.incomeassesment.converter.DisbursementPayloadConverter;
import com.iexceed.appzillonbanking.cagl.incomeassesment.payload.DisbursementPayload;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "tb_ualn_amldetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbUalnAmldetails {
    @Id
    @Column(name = "application_id")
    private String applicationId;

    @JsonProperty("initiatedId")
    @Column(name = "initiated_id")
    private String initiatedId;

    @JsonProperty("initiatedBy")
    @Column(name = "initiated_by")
    private String initiatedBy;

//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("initiatedPayload")
    @Column(name = "initiated_payload", columnDefinition = "text")
    @Convert(converter = DisbursementPayloadConverter.class)
    private DisbursementPayload initiatedPayload;

    @JsonProperty("initiatedDate")
    @Column(name = "initiated_date")
    private LocalDate initiatedDate;

    @JsonProperty("disbId")
    @Column(name = "disb_id")
    private String disbId;

    @JsonProperty("disbBy")
    @Column(name = "disb_by")
    private String disbBy;

//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("disbPayload")
    @Column(name = "disb_payload", columnDefinition = "text")
    @Convert(converter = DisbursementPayloadConverter.class)
    private DisbursementPayload disbPayload;

    @JsonProperty("disbDate")
    @Column(name = "disb_date")
    private LocalDate disbDate;

    @JsonProperty("customerId")
    @Column(name = "customer_id")
    private String customerId;

    @JsonProperty("branchId")
    @Column(name = "branch_id")
    private String branchId;

    @JsonProperty("createdBy")
    @Column(name = "created_by")
    private String createdBy;

    @JsonProperty("createdTs")
    @Column(name = "created_ts")
    private Timestamp createdTs;

    @JsonProperty("updatedBy")
    @Column(name = "updated_by")
    private String updatedBy;

    @JsonProperty("updatedTs")
    @Column(name = "updated_ts")
    private Timestamp updatedTs;

    // initiated name, disbursement name, member id
}
