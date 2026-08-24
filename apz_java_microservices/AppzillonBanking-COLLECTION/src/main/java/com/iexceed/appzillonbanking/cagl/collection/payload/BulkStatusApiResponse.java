package com.iexceed.appzillonbanking.cagl.collection.payload;

import lombok.Data;

import java.util.List;

@Data
public class BulkStatusApiResponse {
    private boolean status;
    private String message;
    private int statusCode;
    private List<TxnData> data;
}
