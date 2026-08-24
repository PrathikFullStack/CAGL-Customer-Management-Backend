package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private String status;          // SUCCESS | FAILURE
    private String message;
    private String applicationId;
    private String customerId;
    private String stage;
    private String subStage;
    private Integer versionNum;

    public static ApiResponse success(String message, String applicationId, String customerId,
                                       String stage, String subStage, Integer versionNum) {
        return ApiResponse.builder()
                .status("SUCCESS")
                .message(message)
                .applicationId(applicationId)
                .customerId(customerId)
                .stage(stage)
                .subStage(subStage)
                .versionNum(versionNum)
                .build();
    }

    public static ApiResponse failure(String message) {
        return ApiResponse.builder()
                .status("FAILURE")
                .message(message)
                .build();
    }

    @Override
    public String toString() {
        return "{" +
                    "status: " + status + "," +
                    "message: " + message + "," +
                    "applicationId: " + applicationId + "," +
                    "customerId: " + customerId + "," +
                    "stage: " + stage + "," +
                    "subStage: " + subStage + "," +
                    "versionNum: " + versionNum +
                "}";
    }
}
