package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;

import java.io.Serializable;

public class TbObBMReinterviewId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "reinterview_id", nullable = false)
    @JsonProperty("reinterviewId")
    private Long reinterviewId;

    @Column(name = "application_id", nullable = false)
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "customer_id", nullable = false)
    @JsonProperty("customerId")
    private String customerId;
}
