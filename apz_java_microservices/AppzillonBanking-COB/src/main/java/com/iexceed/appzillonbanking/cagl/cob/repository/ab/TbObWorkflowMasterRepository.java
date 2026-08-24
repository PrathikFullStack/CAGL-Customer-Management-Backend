package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObWorkflowMaster.TbObWorkflowMasterId;

@Repository
public interface TbObWorkflowMasterRepository
        extends JpaRepository<TbObWorkflowMaster, TbObWorkflowMasterId> {

    Optional<TbObWorkflowMaster> findByAppIdAndWorkflowId(String appId, String workflowId);
}
