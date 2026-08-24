package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.ApplicationWorkflowHis;
import com.iexceed.appzillonbanking.cagl.document.domain.ab.ApplicationWorkflowHisId;

@Repository
public interface ApplicationWorkflowHisRepository extends CrudRepository<ApplicationWorkflowHis, ApplicationWorkflowHisId> {


    @Query(value = "SELECT * FROM public.TB_UAWF_APPLN_WORKFLOW_HISTORY a where a.application_id =:applicationId ORDER BY a.created_ts DESC LIMIT 1 ", nativeQuery = true)
    Optional<ApplicationWorkflowHis> findlatestWorkflowDetails(@Param("applicationId") String applicationId);



    Optional<ApplicationWorkflowHis> findTopByAppIdAndApplicationIdAndVersionNumOrderByWorkflowSeqNumDesc(String appId, String applicationId, int versionNum);

    List<ApplicationWorkflowHis> findByAppIdAndApplicationIdAndApplicationStatusOrderByCreateTsAsc(String appId, String applicationId, String applicationrejectedstatus);

    List<ApplicationWorkflowHis> findByApplicationIdAndApplicationStatusIn(String applicationId, List<String> statusList);

    Optional<ApplicationWorkflowHis> findTopByAppIdAndApplicationIdOrderByWorkflowSeqNumDesc(String appId,
                                                                                             String applicationId);

    Optional<ApplicationWorkflowHis> findTopByApplicationId(String applicationId);

    Optional<ApplicationWorkflowHis> findTopByApplicationIdOrderByCreateTsDesc(String applicationId);

    @Query(value = "SELECT * FROM public.TB_UAWF_APPLN_WORKFLOW_HISTORY where application_id =:applicationId and application_status = 'INITDISBURSE' and created_ts =(select max(created_ts) FROM public.tb_uawf_appln_workflow_history where application_id =:applicationId and application_status = 'INITDISBURSE') ", nativeQuery = true)
    Optional<ApplicationWorkflowHis> findCreatedByUsingApplicationIdAndApplicationStatus(String applicationId);

//	@Query(value = "SELECT * FROM ( SELECT DISTINCT created_by, application_id, workflow_seq_no, application_status FROM public.TB_UAWF_APPLN_WORKFLOW_HISTORY WHERE application_id =:applicationId AND application_status = 'SANCTIONINPROGRESS')", nativeQuery = true)
//	List<ApplicationWorkflowHis> findCreatedByUsingApplicationIdAndApplicationStatusForSanction(String applicationId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE public.TB_UAWF_APPLN_WORKFLOW_HISTORY SET application_id = :updatedApplicationId WHERE application_id = :originalApplicationId", nativeQuery = true)
    void updateApplicationId(@Param("originalApplicationId") String originalApplicationId, @Param("updatedApplicationId") String updatedApplicationId);
















}
