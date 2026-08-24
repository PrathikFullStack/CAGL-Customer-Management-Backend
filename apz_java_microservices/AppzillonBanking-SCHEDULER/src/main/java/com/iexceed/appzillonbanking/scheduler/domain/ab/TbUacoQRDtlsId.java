package com.iexceed.appzillonbanking.scheduler.domain.ab;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbUacoQRDtlsId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appId;

    private String customerId;

    private String billNumber;
}
