package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplnWorkflowEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplnWorkflowPK;

@Repository
public interface CmApplnWorkflowRepository extends JpaRepository<CmApplnWorkflowEntity, CmApplnWorkflowPK> {

    List<CmApplnWorkflowEntity> findByApplicationIdOrderByWorkflowSeqNoDesc(String applicationId);

    @Query("SELECT MAX(w.workflowSeqNo) FROM CmApplnWorkflowEntity w WHERE w.applicationId = :applicationId")
    Optional<Integer> findMaxSequenceNo(@Param("applicationId") String applicationId);
}
