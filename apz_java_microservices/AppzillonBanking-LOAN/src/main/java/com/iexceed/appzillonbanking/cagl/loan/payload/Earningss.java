package com.iexceed.appzillonbanking.cagl.loan.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Earningss {

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("rowNo")
    private String rowNo;

    @JsonProperty("name")
    private String name;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("memRelation")
    private String memRelation;

    @JsonProperty("legaldocName")
    private String legaldocName;

    @JsonProperty("legaldocId")
    private String legaldocId;

    @JsonProperty("OCRresponselog")
    private String OCRresponselog;

    @JsonProperty("OCRData")
    private OCRDatas OCRData;

    @JsonProperty("InputData")
    private InputDatas InputData;

    @JsonProperty("ValidateResp")
    private ValidateResp ValidateResp;

    @JsonProperty("forntOCRlogs")
    private Object forntOCRlogs;

    @JsonProperty("backOCRlogs")
    private Object backOCRlogs;

    @JsonProperty("isKycEdited")
    private Boolean isKycEdited;

    @JsonProperty("isEarning")
    private Boolean isEarning;

    @JsonProperty("isEdited")
    private Boolean isEdited;
    
    @JsonProperty("isReUploadKM")
    private Boolean isReUploadKM;

    @JsonProperty("frontImg")
    private String frontImg;

    @JsonProperty("backImg")
    private String backImg;

    @JsonProperty("status")
    private String status;

    @JsonProperty("EarnmemCount")
    private Integer EarnmemCount;

    @JsonProperty("EarnmemName")
    private String EarnmemName;
    
    @JsonProperty("docuNoF")
    private String docuNoF;
    
    @JsonProperty("docuNoB")
    private String docuNoB;
         
}