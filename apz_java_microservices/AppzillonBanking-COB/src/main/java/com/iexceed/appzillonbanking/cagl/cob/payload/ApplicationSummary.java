package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummary {
    private String applicationId;
    private String customerId;
    private String customerName;
    private String kendraId;
    private String kendraName;
    private String groupId;
    private String groupName;
    private String branchId;
    private String branchName;
    private String leader;
    private String createdBy;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss.SSS")
    private LocalDateTime createdTs;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss.SSS")
    private LocalDateTime updatedTs;
    private String updatedBy;
    private String kmId;
    private String stage;
    private String subStage;
    private String status;
    private String remarks;
    private String custLabel;
}