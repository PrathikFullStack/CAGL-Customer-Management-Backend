    package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.stereotype.Component;

    import java.sql.Timestamp;
    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Component
    @Table(name = "TB_OB_CB_RESPONSE")
    public class TbObCbResponse {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "seq_id")
        private Long seqId;

        @JsonProperty("applicationId")
        @Column(name = "application_id")
        private String applicationId;

        @JsonProperty("mobileNo")
        @Column(name = "mobile_no")
        private String mobileNo;

        @JsonProperty("customerId")
        @Column(name = "customer_id")
        private String customerId;

        @JsonProperty("userRole")
        @Column(name = "user_role")
        private String userRole;

        @JsonProperty("apiName")
        @Column(name = "api_name")
        private String apiName;

        @JsonProperty("idType")
        @Column(name = "id_type")
        private String idType;

        @JsonProperty("uId")
        @Column(name = "uid")
        private String uid;

        @JsonProperty("apiReqTs")
        @Column(name = "api_req_ts")
        private LocalDateTime apiReqTs;

        @JsonProperty("apiResTs")
        @Column(name = "api_res_ts")
        private LocalDateTime apiResTs;

        @JsonProperty("requestPayload")
        @Column(name = "request_payload")
        private String requestPayload;

        @JsonProperty("responsePayload")
        @Column(name = "response_payload")
        private String responsePayload;

        @JsonProperty("status")
        @Column(name = "status")
        private String status;

        @JsonProperty("apiStatus")
        @Column(name = "api_status")
        private String apiStatus;

        @JsonProperty("schedulerStatus")
        @Column(name = "scheduler_status")
        private String schedulerStatus;

        @JsonProperty("appVer")
        @Column(name = "app_ver")
        private String appVer;

        @JsonProperty("addInfo1")
        @Column(name = "add_info1")
        private String addInfo1;

        @JsonProperty("addInfo2")
        @Column(name = "add_info2")
        private String addInfo2;

        @JsonProperty("retryCount")
        @Column(name = "retry_count")
        private String retryCount;

        @JsonProperty("createTs")
        @Column(name = "create_ts")
        private Timestamp createTs;
    }
