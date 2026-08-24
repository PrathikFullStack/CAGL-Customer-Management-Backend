package com.iexceed.appzillonbanking.cagl.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GkNomineeDTO {

    private String custid;
    private String nomineeName;
    private String applicationId;
    private String legaldocId;
    private String dob;
    private String memRelation;
    private String legaldocName;
    private String mobileNum;
    private String gender;
    private String docuNoF;
    private String docuNoB;
    private String createdAt;
    @JsonIgnore
    private String updatedAt;
    private String status;
    
}
