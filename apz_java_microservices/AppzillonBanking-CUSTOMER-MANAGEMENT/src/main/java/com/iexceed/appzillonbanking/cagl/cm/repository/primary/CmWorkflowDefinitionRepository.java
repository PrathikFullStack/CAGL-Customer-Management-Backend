package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowDefinitionEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowDefinitionPK;

@Repository
public interface CmWorkflowDefinitionRepository extends JpaRepository<CmWorkflowDefinitionEntity, CmWorkflowDefinitionPK> {

    Optional<CmWorkflowDefinitionEntity> findByAppIdAndWorkflowIdAndFromStageIdAndAction(
            String appId, String workflowId, String fromStageId, String action);

    List<CmWorkflowDefinitionEntity> findByAppIdAndWorkflowId(String appId, String workflowId);
}
