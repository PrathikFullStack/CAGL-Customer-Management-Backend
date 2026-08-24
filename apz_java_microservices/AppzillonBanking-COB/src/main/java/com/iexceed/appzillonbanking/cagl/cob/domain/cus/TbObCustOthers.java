package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.utils.StringSequenceGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tb_ob_cust_others")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbObCustOthers {

    @Id
    @StringSequenceGenerator(sequenceName = "seq_ob_cust_others_id")
    @Column(name = "cust_others_id", nullable = false)
    @JsonProperty("custOthersId")
    private String custOtherId;

    @Column(name = "customer_id", nullable = false)
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "application_id", nullable = false)
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "attendance", columnDefinition = "TEXT")
//    @Column(name = "attendance", columnDefinition = "jsonb")
//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("attendance")
//    private List<Map<String, Object>> attendance;
    private String attendance;

    @Column(name = "incomedet", columnDefinition = "TEXT")
    @JsonProperty("incomedet")
    private String incomedet;

    @Column(name = "questionnaire", columnDefinition = "TEXT")
//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("questionnaire")
//    private List<Map<String, Object>> learningSession;
    private String questionnaire;

    @Column(name = "peradddet", columnDefinition = "TEXT")
    @JsonProperty("peradddet")
    private String peradddet;


    @Column(name = "learning_session", columnDefinition = "TEXT")
//    @Column(name = "learning_session", columnDefinition = "jsonb")
//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("learningSession")
//    private List<Map<String, Object>> learningSession;
    private String learningSession;

    @Column(name = "cgt_info", columnDefinition = "TEXT")
//    @Column(name = "cgt_info", columnDefinition = "jsonb")
//    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("cgtInfo")
//    private Map<String, Object> cgtInfo;
    private String cgtInfo;

    @Column(name = "grt_info", columnDefinition="TEXT")
    @JsonProperty("grtInfo")
    private String grtInfo;

    @Column(name = "add_info", columnDefinition="TEXT")
    @JsonProperty("addInfo")
    private String addInfo;

    @Column(name = "created_ts")
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;

    @Column(name = "updated_by")
    @JsonProperty("updatedBy")
    private String updatedBy;
}
