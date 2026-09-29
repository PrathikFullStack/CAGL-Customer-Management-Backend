package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;

public interface CdhIngestionService {

    /**
     * Ingests a customer from CDH (gk_unified_data) into normalized tb_cm_* tables
     *
     * @param customerId Customer identifier / Member ID
     * @param userId     Initiator user ID
     * @return Ingested customer entity
     */
    Optional<CmCustomerEntity> ingestFromCdh(String customerId, String userId);
}
