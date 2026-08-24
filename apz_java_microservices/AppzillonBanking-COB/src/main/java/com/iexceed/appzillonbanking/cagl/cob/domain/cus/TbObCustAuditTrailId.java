package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TbObCustAuditTrailId implements Serializable {

    @Column(name = "app_id", length = 30)
    private String appId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "customer_id", length = 20)
    private String customerId;

    @Column(name = "kendra_id", length = 20)
    private String kendraId;

    @Column(name = "group_id", length = 20)
    private String groupId;
}