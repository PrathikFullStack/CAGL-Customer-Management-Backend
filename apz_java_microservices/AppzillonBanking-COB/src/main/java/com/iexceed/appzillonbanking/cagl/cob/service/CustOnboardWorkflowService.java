package com.iexceed.appzillonbanking.cagl.cob.service;

import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObWorkflowDefinitionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowDefinition;

@Service
public class CustOnboardWorkflowService {

    private static final Logger logger = LogManager.getLogger(CustOnboardWorkflowService.class);

    @Autowired
    private TbObWorkflowDefinitionRepository tbObWorkflowDefinitionRepository;

    /**
     * @param appId       e.g. "APZCBO"
     * @param workflowId  e.g. "OBKMINPUT"
     * @param fromStageId current stage, e.g. "BRESUCCESS" — ignored if seqNo is supplied
     * @param action      e.g. "SUBMIT" — ignored if seqNo is supplied
     *                     if non-blank, looks up by (appId, workflowId, stageSeqNo)
     *                    instead of (appId, workflowId, fromStageId, action).
     *                    Leave blank/null for the onboarding flow as designed.
     */
    public TbObWorkflowDefinition getWorkflowDefinition(String appId, String workflowId, String fromStageId,
            String action) {

        logger.debug("Inside getWorkflowDefinition - appId:" + appId + ", workflowId:" + workflowId
                + ", fromStageId:" + fromStageId + ", action:" + action);
        Optional<TbObWorkflowDefinition> workflowRS = tbObWorkflowDefinitionRepository
                .findByAppIdAndWorkflowIdAndFromStageIdAndAction(appId, workflowId, fromStageId, action);
        logger.debug("Repository response:{}", workflowRS);
        return workflowRS.orElse(null);
    }
}
