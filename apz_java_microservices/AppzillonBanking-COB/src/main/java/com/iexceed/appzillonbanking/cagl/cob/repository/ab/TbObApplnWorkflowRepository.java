package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObApplnWorkflowRepository extends JpaRepository<TbObApplnWorkflow, TbObApplnWorkflowId> {

    long countByApplicationId(String applicationId);

    List<TbObApplnWorkflow> findByApplicationIdOrderByVersionNoAsc(String applicationId);

    Optional<TbObApplnWorkflow> findByApplicationId(String applicationId);

    //    @Modifying
//    @Query("update TbObAppWorkflow v set v.isLatest = false " +
//            "where v.applicationId = :applicationId")
//    int supersedeCurrentVersion(@Param("applicationId") String applicationId);

    Optional<TbObApplnWorkflow> findTopByAppIdAndApplicationIdAndVersionNoOrderByWorkflowSeqNoDesc(
            String appId, String applicationId, Integer versionNo);

    Optional<TbObApplnWorkflow> findTopByApplicationIdOrderByCreatedTsDesc(String applicationId);
}