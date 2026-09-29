package com.iexceed.appzillonbanking.cagl.cm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.cdh.GkUnifiedDataEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchResultDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.cdh.GkUnifiedDataRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerSearchService;

@Service
public class CustomerSearchServiceImpl implements CustomerSearchService {

    private static final Logger logger = LogManager.getLogger(CustomerSearchServiceImpl.class);

    private final GkUnifiedDataRepository cdhRepo;
    private final CmApplicationMasterRepository appRepo;

    public CustomerSearchServiceImpl(
            GkUnifiedDataRepository cdhRepo,
            CmApplicationMasterRepository appRepo) {
        this.cdhRepo = cdhRepo;
        this.appRepo = appRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerSearchResultDto> searchCustomers(CustomerSearchRequest request) {
        String keyword = request.getSearchValue();
        logger.info("Executing customer search for keyword: {}", keyword);

        List<CustomerSearchResultDto> results = new ArrayList<>();

        // 1. Search in Central Data Hub (CDH)
        List<GkUnifiedDataEntity> cdhList = cdhRepo.searchByKeyword(keyword);
        for (GkUnifiedDataEntity cdh : cdhList) {
            results.add(CustomerSearchResultDto.builder()
                    .customerId(cdh.getCustomerId())
                    .customerName(cdh.getCustomerName())
                    .mobileNumber(cdh.getMobileNumber())
                    .primaryKycType(cdh.getPrimaryType())
                    .primaryKycId(cdh.getPrimaryId())
                    .kendraId(cdh.getKendraId() != null ? cdh.getKendraId().toString() : null)
                    .kendraName(cdh.getKendraName())
                    .branchName(cdh.getBranchName())
                    .customerStatus(cdh.getCustomerStatus())
                    .sourceSystem("CDH")
                    .activeLoanCount(cdh.getLoanId() != null ? "1" : "0")
                    .overdueStatus(cdh.getOverdueStatus())
                    .build());
        }

        // 2. Search in Local Applications if results from CDH are empty
        if (results.isEmpty()) {
            List<CmApplicationMasterEntity> localApps = appRepo.searchByKeyword(keyword);
            for (CmApplicationMasterEntity app : localApps) {
                results.add(CustomerSearchResultDto.builder()
                        .customerId(app.getCustomerId())
                        .customerName(app.getCustomerName())
                        .mobileNumber(app.getMobileNumber())
                        .primaryKycType("AADHAAR")
                        .primaryKycId(null)
                        .kendraId(app.getKendraId())
                        .kendraName(app.getKendraName())
                        .branchName(app.getBranchName())
                        .customerStatus(app.getStatus())
                        .sourceSystem("LOCAL_CM")
                        .activeLoanCount(app.getLoanId() != null ? "1" : "0")
                        .overdueStatus("0 DPD")
                        .build());
            }
        }

        return results;
    }
}
