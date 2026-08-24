package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cagl.cob.utils.StringSequenceGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tb_ob_customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "customerId")
@Builder
public class TbObCustomer {

    @Id
    @StringSequenceGenerator(sequenceName = "seq_ob_customer_id")
    @Column(name = "customer_id", nullable = false, updatable = false, length = 50)
    private String customerId;

    @Column(name = "application_id")
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "customer_name")
    @JsonProperty("customerName")
    private String customerName;

    @Column(name = "dob")
    @JsonProperty("dob")
    private String dob;

    @Column(name = "marital_status")
    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @Column(name = "live_photo_status")
    @JsonProperty("livePhotoStatus")
    private String livePhotoStatus;

    @Column(name = "kyc_status")
    @JsonProperty("kycStatus")
    private String kycStatus;

    @Column(name = "primary_kyc_type")
    @JsonProperty("primaryKycType")
    private String primaryKycType;

    @Column(name = "primary_kyc_id")
    @JsonProperty("primaryKycId")
    private String primaryKycId;

    @Column(name = "meeting_day")
    @JsonProperty("meetingDay")
    private String meetingDay;

    @Column(name = "distance_from_kendra")
    @JsonProperty("distanceFromKendra")
    private String distanceFromKendra;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JsonProperty("payload")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @Column(name = "kyc_details", columnDefinition = "jsonb")
    @JsonProperty("kycDetails")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> kycDetails;

    @Column(name = "bank_details", columnDefinition = "jsonb")
    @JsonProperty("bankDetails")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> bankDetails;

    @Column(name = "verification_det", columnDefinition = "jsonb")
    @JsonProperty("verificationDet")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> verificationDet;

    @Column(name = "photo_doc_id")
    @JsonProperty("photoDocId")
    private String photoDocId;

//    @Column(name = "mobile_number")
//    @JsonProperty("mobileNumber")
//    private String mobileNumber;

//    @Column(name = "aml_status")
//    @JsonProperty("amlStatus")
//    private String amlStatus;

//    @Column(name = "bre_status")
//    @JsonProperty("breStatus")
//    private String breStatus;

//    @Column(name = "channel_type")
//    @JsonProperty("channelType")
//    private String channelType;

//    @Column(name = "is_km_edited")
//    @JsonProperty("isKmEdited")
//    private String isKmEdited;

//    @Column(name = "record_type")
//    @JsonProperty("recordType")
//    private String recordType;

    @Column(name = "created_by")
    @JsonProperty("createdBy")
    private String createdBy;

    @Column(name = "created_ts")
    @JsonProperty("createdTs")
    private LocalDateTime createdTs;

    @Column(name = "updated_by")
    @JsonProperty("updatedBy")
    private String updatedBy;

    @Column(name = "updated_ts")
    @JsonProperty("updatedTs")
    private LocalDateTime updatedTs;
}