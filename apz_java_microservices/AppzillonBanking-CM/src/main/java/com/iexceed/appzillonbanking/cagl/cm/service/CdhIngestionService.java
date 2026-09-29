package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;

public interface CdhIngestionService {


    Optional<CmCustomerEntity> ingestFromCdh(String customerId, String userId);
}
