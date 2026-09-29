package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchResultDto;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cm/search")
@Tag(name = "2. Customer Search", description = "Endpoints for Global and Local Customer Search across CDH and Local CM DB")
public class CustomerSearchRestController {

    private static final Logger logger = LogManager.getLogger(CustomerSearchRestController.class);

    private final CustomerSearchService searchService;

    public CustomerSearchRestController(CustomerSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    @Operation(summary = "Search Customer by Keyword", description = "Searches CDH Central Data Hub (gk_unified_data) and local database by Customer ID, Kendra ID, Mobile Number, or KYC ID")
    public ResponseEntity<ResponseWrapper<List<CustomerSearchResultDto>>> searchCustomer(
            @RequestBody RequestWrapper<CustomerSearchRequest> request) {
        CustomerSearchRequest searchReq = request.getBody();
        logger.info("Search request received for Keyword: {}", searchReq.getSearchValue());

        List<CustomerSearchResultDto> results = searchService.searchCustomers(searchReq);
        return ResponseEntity.ok(ResponseWrapper.success(results, "Search completed successfully"));
    }
}
