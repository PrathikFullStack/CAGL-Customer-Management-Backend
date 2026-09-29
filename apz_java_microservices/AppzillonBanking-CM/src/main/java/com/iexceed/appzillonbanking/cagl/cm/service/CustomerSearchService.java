package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchResultDto;

public interface CustomerSearchService {

    /**
     * Searches for customers across CDH Central Data Hub and Local CM Database
     *
     * @param request Search request containing query keyword
     * @return List of matching customer search result DTOs
     */
    List<CustomerSearchResultDto> searchCustomers(CustomerSearchRequest request);
}
