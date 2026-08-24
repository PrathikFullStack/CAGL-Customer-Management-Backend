package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.WorkflowDefinition;
import com.iexceed.appzillonbanking.cagl.document.domain.ab.WorkflowDefinitionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowDefinitionHisRepository extends CrudRepository<WorkflowDefinition, WorkflowDefinitionId> {

    List<WorkflowDefinition> findByFromStageId(String nextWorkFlowStage);
}
