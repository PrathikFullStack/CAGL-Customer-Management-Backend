package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowDefinition;
import org.springframework.stereotype.Repository;

@Repository
public interface TbObWorkflowDefinitionRepository
		extends JpaRepository<TbObWorkflowDefinition, TbObWorkflowDefinition.TbObWorkflowDefinitionId> {



	List<TbObWorkflowDefinition> findByCurrRole(String currRole);
/**
* Resolves the single transition row that matches: "from this workflow, sitting in
* this from-stage, taking this action - where does the application go next, and
* what sequence number is this step?"
*/
Optional<TbObWorkflowDefinition> findByWorkflowIdAndFromStageIdAndAction(
    String workflowId, String fromStageId, String action);

    Optional<TbObWorkflowDefinition> findByAppIdAndWorkflowIdAndFromStageIdAndAction(
            String appId, String workflowId, String fromStageId, String action);

}