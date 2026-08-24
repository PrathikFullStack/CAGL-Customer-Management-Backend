package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.utils.StringSequenceGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tb_ob_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObAddress {

    @Id
    @StringSequenceGenerator(sequenceName = "seq_ob_address_id")
    @Column(name = "address_id")
    @JsonProperty("addressId")
    private String addressId;

    @Column(name = "customer_id")
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "application_id")
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "address_type")
    @JsonProperty("addressType")
    private String addressType;

    @Column(name = "comm_same_as_perm")
    @JsonProperty("commSameAsPerm")
    private String commSameAsPerm;

    @Column(name = "addr_payload", columnDefinition = "jsonb")
    @JsonProperty("addrPayload")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> addrPayload;

    @Column(name = "address_proof_type")
    @JsonProperty("addressProofType")
    private String addressProofType;

    @Column(name = "address_proof_doc_id")
    @JsonProperty("addressProofDocId")
    private String addressProofDocId;

//    @Column(name = "gps_latitude")
//    @JsonProperty("gpsLatitude")
//    private BigDecimal gpsLatitude;

//    @Column(name = "gps_longitude")
//    @JsonProperty("gpsLongitude")
//    private BigDecimal gpsLongitude;

    @Column(name = "distance_from_branch")
    @JsonProperty("distanceFromBranch")
    private BigDecimal distanceFromBranch;

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