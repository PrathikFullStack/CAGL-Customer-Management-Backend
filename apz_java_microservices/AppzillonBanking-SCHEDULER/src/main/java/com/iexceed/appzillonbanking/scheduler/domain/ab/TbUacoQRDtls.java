package com.iexceed.appzillonbanking.scheduler.domain.ab;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_UACO_QR_DETAILS")
@IdClass(TbUacoQRDtlsId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbUacoQRDtls {

    @Id
    @Column(name = "app_id")
    private String appId;

    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Id
    @Column(name = "bill_number")
    private String billNumber;

    @Column(name = "maitri_status")
    private String maitriStatus;
}
