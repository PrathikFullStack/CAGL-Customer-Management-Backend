package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.constants.CmConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseMessageConstants;
import com.iexceed.appzillonbanking.cagl.cm.entity.cdh.GkUnifiedDataEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchResultDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.cdh.GkUnifiedDataRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;

@RestController
@RequestMapping(CmConstants.API_SEARCH)
public class CustomerSearchRestController {

    private static final Logger logger = LogManager.getLogger(CustomerSearchRestController.class);

    private final GkUnifiedDataRepository cdhRepo;
    private final CmApplicationMasterRepository appRepo;

    public CustomerSearchRestController(
            GkUnifiedDataRepository cdhRepo,
            CmApplicationMasterRepository appRepo) {
        this.cdhRepo = cdhRepo;
        this.appRepo = appRepo;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<List<CustomerSearchResultDto>>> searchCustomer(
            @RequestBody RequestWrapper<CustomerSearchRequest> request) {
        CustomerSearchRequest searchReq = request.getBody();
        logger.info("Search request received for Keyword: {}", searchReq.getSearchValue());

        List<CustomerSearchResultDto> results = new ArrayList<>();
        List<GkUnifiedDataEntity> cdhList = cdhRepo.searchByKeyword(searchReq.getSearchValue());

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
                    .sourceSystem(CmConstants.SOURCE_CDH)
                    .activeLoanCount(cdh.getLoanId() != null ? CmConstants.FLAG_TRUE : CmConstants.FLAG_FALSE)
                    .overdueStatus(cdh.getOverdueStatus())
                    .build());
        }

        return ResponseEntity.ok(ResponseWrapper.success(results, ResponseMessageConstants.MSG_SEARCH_COMPLETED));
    }
}
