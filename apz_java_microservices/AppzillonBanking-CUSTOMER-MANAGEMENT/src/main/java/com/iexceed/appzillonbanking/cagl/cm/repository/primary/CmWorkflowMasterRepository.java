package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmWorkflowMasterPK;

@Repository
public interface CmWorkflowMasterRepository extends JpaRepository<CmWorkflowMasterEntity, CmWorkflowMasterPK> {
}
