package com.iexceed.appzillonbanking.cagl.cob.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplnWorkflowRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowDefinition;

@Service
public class CustOnboardApplnWorkflowService {


    private static final Logger logger = LogManager.getLogger(CustOnboardApplnWorkflowService.class);

    @Autowired
    private TbObApplnWorkflowRepository tbObApplnWorkflowRepository;

    @Autowired
    private TbObApplicationMasterRepository tbObApplicationMasterRepository;

    public TbObApplnWorkflow recordTransition(String appId, String applicationId, String userId,
                                              TbObWorkflowDefinition definition, String presentRole) {

        TbObApplicationMaster master = tbObApplicationMasterRepository.findById(applicationId)
                .orElseThrow(() -> {
                    logger.error("No tb_ob_application_master record found for applicationId:{}", applicationId);
                    return new IllegalStateException(
                            "No application master record found for applicationId: " + applicationId);
                });
        Integer versionNo = Integer.parseInt(master.getVersion());
        int workflowSeqNo = 1;
        Optional<TbObApplnWorkflow> latest = tbObApplnWorkflowRepository
                .findTopByAppIdAndApplicationIdAndVersionNoOrderByWorkflowSeqNoDesc(appId, applicationId, versionNo);
        if (latest.isPresent()) {
            workflowSeqNo = latest.get().getWorkflowSeqNo() + 1;
        }

        TbObApplnWorkflow row = TbObApplnWorkflow.builder()
                .appId(appId)
                .applicationId(applicationId)
                .versionNo(versionNo)
                .workflowSeqNo(workflowSeqNo)
                .applicationStatus(definition.getNextWorkflowStatus())
                .nextWorkflowStage(definition.getNextStageId())
                .presentRole(presentRole)
                .createdTs(LocalDateTime.now())
                .createdBy(userId)
                .createdUsername(userId)
                .build();

        logger.debug("Recording appln workflow transition :: {}", row);
        return tbObApplnWorkflowRepository.save(row);
    }
}
