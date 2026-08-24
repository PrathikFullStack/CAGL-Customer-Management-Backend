package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateResponse {

    private String applicationId;
    private String stage;
    private String subStage;
    private String status;
    private String channelType;
    private boolean fieldsChanged;
    private List<String> changedFields;
    private LocalDateTime updatedTs;
    private String message;
}