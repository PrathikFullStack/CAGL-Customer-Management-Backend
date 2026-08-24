package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Data;

@Data
public class PaginationRequest {
    private Integer pageNo;
    private Integer pageSize;
    private String sortBy;
    private String sortOrder;
}