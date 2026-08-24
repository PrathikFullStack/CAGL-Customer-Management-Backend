package com.iexceed.appzillonbanking.cagl.cob.domain.ab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObApplnWorkflowId implements Serializable {
    private String appId;
    private String applicationId;
    private Integer versionNo;
    private Integer workflowSeqNo;
}
