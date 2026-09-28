package com.iexceed.appzillonbanking.cagl.cm.payload.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchRequest {
    private String searchType; // CUSTOMER_ID, MOBILE_NO, PRIMARY_KYC, KENDRA_ID, NAME
    private String searchValue;
    private String branchId;
    private Integer pageNo;
    private Integer pageSize;
}
