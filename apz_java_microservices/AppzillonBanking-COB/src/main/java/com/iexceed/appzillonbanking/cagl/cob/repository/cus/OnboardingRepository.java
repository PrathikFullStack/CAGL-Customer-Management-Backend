package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnboardingRepository extends JpaRepository<TbObApplicationMaster, String>, JpaSpecificationExecutor<TbObApplicationMaster>
{
    long countByStage(String stage);
    long countByStageAndBranchId(String stage, String branchId);
    long countByStageAndKendraIdIn(String stage, List<String> kendraIds);
    long countByStageInAndKendraIdIn(List<String> stages, List<String> kendraIds);
    long countByStageInAndBranchId(List<String> stages, String branchId);
    long countByStageAndRecordTypeInAndKendraIdIn(String stage, List<String> recordTypes, List<String> kendraIds);
    long countByStageAndRecordTypeInAndBranchId(String stage, List<String> recordTypes, String branchId);
    long countByStageAndWfStageAndKendraIdIn(String stage, String wfStage, List<String> kendraIds);
    long countByStageAndWfStageAndBranchId(String stage, String wfStage, String branchId);
    long countByRecordTypeAndKendraIdIn(String recordType, List<String> kendraIds);
    long countByUpdatedByAndUpdatedTsAfter(@Param("updatedBy") String updatedBy, @Param("after") LocalDateTime after);
    long countByStageIn(List<String> stages);

    /**
     * Counts applications per case-ageing bucket in one pass. Age is the whole number of days
     * between the application's last activity date (updated_ts, falling back to created_ts) and
     * today, capped at 6 so everything older than 5 days collapses into a single "above 5" bucket.
     * Returns [bucket, count] rows; buckets with no rows are absent.
     */
    @Query(value = "SELECT bucket, COUNT(*) FROM ("
            + "  SELECT LEAST(CURRENT_DATE - CAST(COALESCE(a.updated_ts, a.created_ts) AS DATE), :aboveBucket) AS bucket"
            + "  FROM tb_ob_application_master a"
            + "  WHERE a.stage IN (:stages)"
            + ") t GROUP BY bucket", nativeQuery = true)
    List<Object[]> countCaseAgeingBucketsByStageIn(@Param("stages") List<String> stages,
                                                   @Param("aboveBucket") int aboveBucket);

    Page<TbObApplicationMaster> findByStage(String stage, Pageable pageable);
    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages")
    Page<TbObApplicationMaster> findByStageIn(@Param("stages") List<String> stages, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.kendraId IN :kendraIds ORDER BY a.createdTs ASC")
    Page<TbObApplicationMaster> findDraftsByAllSubCategories(@Param("stages") List<String> stages, @Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.branchId = :branchId ORDER BY a.createdTs ASC")
    Page<TbObApplicationMaster> findDraftsByAllSubCategoriesForBm(@Param("stages") List<String> stages, @Param("branchId") String branchId, Pageable pageable);

    Page<TbObApplicationMaster> findByStageAndBranchId(String stage, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeAndBranchId(String stage, String recordType, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndBranchId(String stage, List<String> recordTypes, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndKendraIdIn(String stage, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeAndKendraIdIn(String stage, String recordType, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndKendraIdIn(String stage, List<String> recordTypes, List<String> kendraIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.wfStage = :wfStage AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findByStageAndWfStageAndKendraIdIn(@Param("stage") String stage, @Param("wfStage") String wfStage, @Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.wfStage = :wfStage AND a.branchId = :branchId")
    Page<TbObApplicationMaster> findByStageAndWfStageAndBranchId(@Param("stage") String stage, @Param("wfStage") String wfStage, @Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findCbFailByStageInAndKendraIdIn(@Param("stages") List<String> stages, @Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.branchId = :branchId")
    Page<TbObApplicationMaster> findCbFailByStageInAndBranchId(@Param("stages") List<String> stages, @Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.stage = 'BMQUEUE' OR a.status = 'BMQUEUE') AND a.recordType = :recordType AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findReinterviewByRecordTypeAndKendraIdIn(@Param("recordType") String recordType, @Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.stage = 'BMQUEUE' OR a.status = 'BMQUEUE') AND a.recordType IN :recordTypes AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findReinterviewByRecordTypeInAndKendraIdIn(@Param("recordTypes") List<String> recordTypes, @Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.stage = 'BMQUEUE' OR a.status = 'BMQUEUE') AND a.recordType IN :recordTypes AND a.kendraId IN :kendraIds")
    long countReinterviewByRecordTypeInAndKendraIdIn(@Param("recordTypes") List<String> recordTypes, @Param("kendraIds") List<String> kendraIds);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.status = 'GRT' OR a.stage = 'GRT') AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findPendingForGrtByKendraIdIn(@Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.status = 'GRT' OR a.stage = 'GRT') AND a.kendraId IN :kendraIds")
    long countPendingForGrtByKendraIdIn(@Param("kendraIds") List<String> kendraIds);

   @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED') AND a.kendraId IN :kendraIds")
    Page<TbObApplicationMaster> findPendingForActivationByKendraIdIn(@Param("kendraIds") List<String> kendraIds, Pageable pageable);

     @Query("SELECT a FROM TbObApplicationMaster a WHERE a.kendraId IN :kendraIds AND a.wfStage = 'T24_ACTIVATED'")
    Page<TbObApplicationMaster> findActivatedByKendraIdIn(@Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.kendraId IN :kendraIds AND a.wfStage = 'T24_ACTIVATED'")
    long countActivatedByKendraIdIn(@Param("kendraIds") List<String> kendraIds);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.kendraId IN :kendraIds AND (a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    Page<TbObApplicationMaster> findRejectedByKendraIdIn(@Param("kendraIds") List<String> kendraIds, @Param("rejectStages") List<String> rejectStages, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.kendraId IN :kendraIds AND (a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    long countRejectedByKendraIdIn(@Param("kendraIds") List<String> kendraIds, @Param("rejectStages") List<String> rejectStages);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.kendraId IN :kendraIds AND (a.wfStage = 'T24_ACTIVATED' OR a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    Page<TbObApplicationMaster> findActivatedOrRejectedByKendraIdIn(@Param("kendraIds") List<String> kendraIds, @Param("rejectStages") List<String> rejectStages, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.wfStage = :wfStage AND a.branchId IN :branchIds")
    long countByStageAndWfStageAndBranchIdIn(@Param("stage") String stage, @Param("wfStage") String wfStage, @Param("branchIds") List<String> branchIds);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.wfStage = :wfStage AND a.branchId IN :branchIds")
    Page<TbObApplicationMaster> findByStageAndWfStageAndBranchIdIn(@Param("stage") String stage, @Param("wfStage") String wfStage, @Param("branchIds") List<String> branchIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.wfStage = :wfStage AND a.branchId IN :branchIds AND a.createdTs < :beforeDate")
    Page<TbObApplicationMaster> findByStageAndWfStageAndBranchIdInAndCreatedTsBefore(@Param("stage") String stage, @Param("wfStage") String wfStage, @Param("branchIds") List<String> branchIds, @Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.branchId IN :branchIds AND ((a.stage = :pendingStage AND a.wfStage = :pendingWfStage) OR (a.stage = 'ONHOLD' AND a.wfStage = 'RPCONHOLD'))")
    Page<TbObApplicationMaster> findRpcPoolAllByBranchIdIn(@Param("pendingStage") String pendingStage, @Param("pendingWfStage") String pendingWfStage, @Param("branchIds") List<String> branchIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.branchId IN :branchIds AND ((a.stage = :pendingStage AND a.wfStage = :pendingWfStage AND a.createdTs < :beforeDate) OR (a.stage = 'ONHOLD' AND a.wfStage = 'RPCONHOLD' AND a.createdTs < :beforeDate))")
    Page<TbObApplicationMaster> findRpcPoolAllByBranchIdInAndCreatedTsBefore(@Param("pendingStage") String pendingStage, @Param("pendingWfStage") String pendingWfStage, @Param("branchIds") List<String> branchIds, @Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.updatedBy = :updatedBy AND a.updatedTs >= :after")
    Page<TbObApplicationMaster> findByUpdatedByAndUpdatedTsAfter(@Param("updatedBy") String updatedBy, @Param("after") LocalDateTime after, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.branchId IN :branchIds")
    long countByStageAndBranchIdIn(@Param("stage") String stage, @Param("branchIds") List<String> branchIds);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.branchId IN :branchIds")
    Page<TbObApplicationMaster> findByStageAndBranchIdIn(@Param("stage") String stage, @Param("branchIds") List<String> branchIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.branchId IN :branchIds AND a.createdTs < :beforeDate")
    Page<TbObApplicationMaster> findByStageAndBranchIdInAndCreatedTsBefore(@Param("stage") String stage, @Param("branchIds") List<String> branchIds, @Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.branchId IN :branchIds")
    Page<TbObApplicationMaster> findByStageInAndBranchIdIn(@Param("stages") List<String> stages, @Param("branchIds") List<String> branchIds, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.branchId IN :branchIds AND a.createdTs < :beforeDate")
    Page<TbObApplicationMaster> findByStageInAndBranchIdInAndCreatedTsBefore(@Param("stages") List<String> stages, @Param("branchIds") List<String> branchIds, @Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.stage IN :stages AND a.branchId IN :branchIds")
    long countByStageInAndBranchIdIn(@Param("stages") List<String> stages, @Param("branchIds") List<String> branchIds);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.channelType = :channelType AND a.branchId IN :branchIds AND a.createdTs >= :since")
    long countByChannelTypeAndBranchIdInAndCreatedTsAfter(@Param("channelType") String channelType, @Param("branchIds") List<String> branchIds, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.channelType = :channelType AND a.branchId IN :branchIds AND a.createdTs >= :since")
    Page<TbObApplicationMaster> findByChannelTypeAndBranchIdInAndCreatedTsAfter(@Param("channelType") String channelType, @Param("branchIds") List<String> branchIds, @Param("since") LocalDateTime since, Pageable pageable);


    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.updatedBy = :updatedBy AND a.updatedTs >= :after AND a.createdTs < :beforeDate")
    Page<TbObApplicationMaster> findByUpdatedByAndUpdatedTsAfterAndCreatedTsBefore(@Param("updatedBy") String updatedBy, @Param("after") LocalDateTime after, @Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);


    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.status = 'GRT' OR a.stage = 'GRT') AND a.branchId = :branchId")
    Page<TbObApplicationMaster> findPendingForGrtByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.status = 'GRT' OR a.stage = 'GRT') AND a.branchId = :branchId")
    long countPendingForGrtByBranchId(@Param("branchId") String branchId);


    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED') AND a.branchId = :branchId")
    Page<TbObApplicationMaster> findPendingForActivationByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED') AND a.branchId = :branchId")
    long countPendingForActivationByBranchId(@Param("branchId") String branchId);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND a.wfStage = 'T24_ACTIVATED'")
    Page<TbObApplicationMaster> findActivatedByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND a.wfStage = 'T24_ACTIVATED'")
    long countActivatedByBranchId(@Param("branchId") String branchId);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND (a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    Page<TbObApplicationMaster> findRejectedByBranchId(@Param("branchId") String branchId, @Param("rejectStages") List<String> rejectStages, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND (a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    long countRejectedByBranchId(@Param("branchId") String branchId, @Param("rejectStages") List<String> rejectStages);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND (a.wfStage = 'T24_ACTIVATED' OR a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    Page<TbObApplicationMaster> findActivatedOrRejectedByBranchId(@Param("branchId") String branchId, @Param("rejectStages") List<String> rejectStages, Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.branchId = :branchId AND (a.wfStage = 'T24_ACTIVATED' OR a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    long countActivatedOrRejectedByBranchId(@Param("branchId") String branchId, @Param("rejectStages") List<String> rejectStages);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.stage = :stage AND a.recordType IN :recordTypes AND a.branchId IN :branchIds")
    long countByStageAndRecordTypeInAndBranchIdIn(@Param("stage") String stage, @Param("recordTypes") List<String> recordTypes, @Param("branchIds") List<String> branchIds);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.status = 'GRT' OR a.stage = 'GRT') AND a.branchId IN :branchIds")
    long countPendingForGrtByBranchIdIn(@Param("branchIds") List<String> branchIds);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE (a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED') AND a.branchId IN :branchIds")
    long countPendingForActivationByBranchIdIn(@Param("branchIds") List<String> branchIds);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.branchId IN :branchIds AND (a.wfStage = 'T24_ACTIVATED' OR a.status = 'REJECTED' OR a.stage IN :rejectStages)")
    long countActivatedOrRejectedByBranchIdIn(@Param("branchIds") List<String> branchIds, @Param("rejectStages") List<String> rejectStages);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED'")
    Page<TbObApplicationMaster> findPendingForActivation(Pageable pageable);

    @Query("SELECT COUNT(a) FROM TbObApplicationMaster a WHERE a.status = 'GRTAPPROVED' OR a.stage = 'GRTAPPROVED'")
    long countPendingForActivation();

}
