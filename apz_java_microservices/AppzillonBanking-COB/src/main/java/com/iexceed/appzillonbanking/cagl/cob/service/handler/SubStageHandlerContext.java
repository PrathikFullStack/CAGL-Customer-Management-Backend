package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubStageHandlerContext {
    private final CustomerUpdateDtls customerUpdateDtls;
    private final String applicationId;
    private final TbObCustomer customer;
    private final String userId;
    private final LocalDateTime now;
    private final TbObApplicationMaster master;
}
