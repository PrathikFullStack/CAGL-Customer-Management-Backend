package com.iexceed.appzillonbanking.cagl.collection.payload;

import lombok.Data;

@Data
public class TxnData {
    private String merchantTranId;
    private String status;
}