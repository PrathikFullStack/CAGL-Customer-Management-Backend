package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.search.CustomerSearchResultDto;

public interface CustomerSearchService {


    List<CustomerSearchResultDto> searchCustomers(CustomerSearchRequest request);
}
