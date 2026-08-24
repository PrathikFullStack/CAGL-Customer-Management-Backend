package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class CustomerDto {
    private Long customerId;
    private String customerName;
    private String dob;
    private String maritalStatus;
    private String primaryKycType;
    private String primaryKycId;
    private String meetingDay;
    private String distanceFromKendra;
    private String mobileNumber;
    private String amlStatus;
    private String breStatus;
    private String channelType;
    private String isKmEdited;
    private String recordType;
    /** Full payload{} - GPS, eKYC, BRE, AML, flags, family count, land details etc. */
    private Map<String, Object> payload;
    private Map<String, Object> kycDetails;
    private Map<String, Object> bankDetails;
    private Map<String, Object> verificationDetails;
}
