package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tb_ob_loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObLoan {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
//    @SequenceGenerator(
//            name = "loan_seq",
//            sequenceName = "seq_ob_loan_id",
//            allocationSize = 1
//    )
    @Column(name = "loan_seq_id")
    @JsonProperty("loanSeqId")
    private Long loanSeqId;

    @Column(name = "application_id")
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "customer_id")
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "loan_id")
    @JsonProperty("loanId")
    private String loanId;

    @Column(name = "amount")
    @JsonProperty("amount")
    private BigDecimal amount;

    @Column(name = "approved_amt")
    @JsonProperty("approvedAmt")
    private BigDecimal approvedAmt;

    @Column(name = "loan_status")
    @JsonProperty("loanStatus")
    private String loanStatus;

    @Column(name = "freq")
    @JsonProperty("freq")
    private String freq;

    @Column(name = "term")
    @JsonProperty("term")
    private String term;

    @Column(name = "product")
    @JsonProperty("product")
    private String product;

    @Column(name = "product_details", columnDefinition = "jsonb")
    @JsonProperty("productDetails")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> productDetails;

    @Column(name = "charges", columnDefinition = "jsonb")
    @JsonProperty("charges")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> charges;

    @Column(name = "bre_trigger_point")
    @JsonProperty("breTriggerPoint")
    private Integer breTriggerPoint;

    @Column(name = "bre_request_id")
    @JsonProperty("breRequestId")
    private String breRequestId;

    @Column(name = "bre_response_status")
    @JsonProperty("breResponseStatus")
    private String breResponseStatus;

    @Column(name = "t24_kendra_id")
    @JsonProperty("t24KendraId")
    private String t24KendraId;

    @Column(name = "t24_group_id")
    @JsonProperty("t24GroupId")
    private String t24GroupId;

    @Column(name = "t24_customer_id")
    @JsonProperty("t24CustomerId")
    private String t24CustomerId;

    @Column(name = "add_info", columnDefinition = "jsonb")
    @JsonProperty("addInfo")
    private String addInfo;

    @Column(name = "created_ts")
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;
}