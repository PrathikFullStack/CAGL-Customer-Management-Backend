package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnboardingRepository extends JpaRepository<TbObApplicationMaster, String>
{
    long countByWfStage(String wfStage);
    long countByStatus(String status);
    long countByWfStageAndBranchId(String wfStage, String branchId);
    long countByStatusAndBranchId(String status, String branchId);
    long countByStatusAndChannelTypeAndBranchId(String status, String channelType, String branchId);
    long countByStageAndBranchId(String stage, String branchId);
    long countByRecordTypeAndBranchId(String recordType, String branchId);
    long countByWfStageInAndBranchId(List<String> wfStages, String branchId);
    long countByRecordTypeInAndBranchId(List<String> recordTypes, String branchId);
    long countByWfStageAndStatusInAndBranchId(String wfStage, List<String> statuses, String branchId);
    long countByStatusAndRecordTypeInAndBranchId(String status, List<String> recordTypes, String branchId);
    long countByWfStageAndKendraIdIn(String wfStage, List<String> kendraIds);
    long countByStatusAndKendraIdIn(String status, List<String> kendraIds);
    long countByWfStageInAndKendraIdIn(List<String> wfStages, List<String> kendraIds);
    long countByRecordTypeInAndKendraIdIn(List<String> recordTypes, List<String> kendraIds);
    long countByWfStageAndStatusInAndKendraIdIn(String wfStage, List<String> statuses, List<String> kendraIds);
    long countByGroupIdIn(List<String> groupIds);
    long countByStatusAndRecordTypeInAndKendraIdIn(String status, List<String> recordTypes, List<String> kendraIds);
    long countByStatusAndChannelTypeAndKendraIdIn(String status, String channelType, List<String> kendraIds);
    long countByStageAndKendraIdIn(String stage, List<String> kendraIds);
    long countByRecordTypeAndKendraIdIn(String recordType, List<String> kendraIds);
    long countByStatusAndUpdatedByAndUpdatedTsAfter(String status, String updatedBy, LocalDateTime startOfDay);
    long countByStatusAndBranchIdIn(String status, List<String> branchIds);


    // New methods for fetching Draft list by wfStage
    Page<TbObApplicationMaster> findByWfStageAndCreatedBy(String wfStage, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageInAndCreatedBy(List<String> wfStages, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndBranchId(String wfStage, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageInAndBranchId(List<String> wfStages, String branchId, Pageable pageable);

    // New method for CRT Dashboard List
    Page<TbObApplicationMaster> findByStatus(String status, Pageable pageable);


    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.wfStage = 'DRAFT' OR a.status IN :otherStatuses) AND a.createdBy = :userId")
    Page<TbObApplicationMaster> findDraftsByAllSubCategories(@Param("otherStatuses") List<String> otherStatuses, @Param("userId") String userId, Pageable pageable);

    @Query("SELECT a FROM TbObApplicationMaster a WHERE (a.wfStage = 'DRAFT' OR a.status IN :otherStatuses) AND a.branchId = :branchId")
    Page<TbObApplicationMaster> findDraftsByAllSubCategoriesForBm(@Param("otherStatuses") List<String> otherStatuses, @Param("branchId") String branchId, Pageable pageable);


    Page<TbObApplicationMaster> findByStatusAndCreatedBy(String status, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusAndCreatedByAndCreatedTsBefore(String status, String createdBy, LocalDateTime beforeDate, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusInAndCreatedByAndCreatedTsBefore(List<String> statuses, String createdBy, LocalDateTime beforeDate, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusInAndCreatedBy(List<String> statuses, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStage(String stage, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusAndChannelTypeAndCreatedBy(String status, String channelType, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStageAndCreatedBy(String stage, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByRecordTypeAndCreatedBy(String recordType, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByRecordTypeInAndCreatedBy(List<String> recordTypes, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStageAndRecordTypeAndCreatedBy(String stage, String recordType, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndCreatedBy(String stage, List<String> recordTypes, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusAndBranchId(String status, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusInAndBranchId(List<String> statuses, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndBranchId(String stage, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByRecordTypeAndBranchId(String recordType, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByRecordTypeInAndBranchId(List<String> recordTypes, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeAndBranchId(String stage, String recordType, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndBranchId(String stage, List<String> recordTypes, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndChannelTypeAndBranchId(String status, String channelType, String branchId, Pageable pageable);
    @Query("SELECT app FROM TbObApplicationMaster app WHERE app.status IN :statuses AND app.branchId = :branchId AND app.updatedTs >= current_date")
    Page<TbObApplicationMaster> findByStatusInAndBranchIdForCurrentDay(@Param("statuses") List<String> statuses, @Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT app FROM TbObApplicationMaster app WHERE app.status IN :statuses AND app.createdBy = :userId AND app.updatedTs >= current_date")
    Page<TbObApplicationMaster> findByStatusInAndCreatedByForCurrentDay(@Param("statuses") List<String> statuses, @Param("userId") String userId, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusAndCreatedByAndLoanIdIsNull(String status, String createdBy, Pageable pageable);

    Page<TbObApplicationMaster> findByStatusAndKendraIdIn(String status, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusInAndKendraIdIn(List<String> statuses, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndKendraIdIn(String stage, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndKendraIdInAndLoanIdIsNull(String status, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndBranchIdAndLoanIdIsNull(String status, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByRecordTypeAndKendraIdIn(String recordType, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByRecordTypeInAndKendraIdIn(List<String> recordTypes, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeAndKendraIdIn(String stage, String recordType, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndKendraIdIn(String stage, List<String> recordTypes, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndKendraIdIn(String wfStage, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageInAndKendraIdIn(List<String> wfStages, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndStatusAndKendraIdIn(String wfStage, String status, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndStatusInAndKendraIdIn(String wfStage, List<String> statuses, List<String> kendraIds, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndStatusAndBranchId(String wfStage, String status, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByWfStageAndStatusInAndBranchId(String wfStage, List<String> statuses, String branchId, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndBranchIdIn(String status, List<String> branchIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusInAndBranchIdIn(List<String> statuses, List<String> branchIds, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndBranchIdInAndCreatedTsBefore(String status, List<String> branchIds, LocalDateTime beforeDate, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusInAndBranchIdInAndCreatedTsBefore(List<String> statuses, List<String> branchIds, LocalDateTime beforeDate, Pageable pageable);
    Page<TbObApplicationMaster> findByStatusAndChannelTypeAndBranchIdIn(String status, String channelType, List<String> branchIds, Pageable pageable);


    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, a.wfStage, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.createdBy = :userId " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy, a.wfStage")
    List<DashboardCountDTO> getCountsByKmIdGrouped(@Param("userId") String userId);

    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, a.wfStage, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.branchId = :branchId " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy, a.wfStage")
    List<DashboardCountDTO> getCountsByBranchIdGrouped(@Param("branchId") String branchId);

    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.wfStage = :wfStage " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy")
    List<DashboardCountDTO> getCountsByWfStageGrouped(@Param("wfStage") String wfStage);

    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.updatedBy = :userId AND a.updatedTs >= current_date " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy")
    List<DashboardCountDTO> getClearedCountsForCurrentUser(@Param("userId") String userId);

    Page<TbObApplicationMaster> findByStatusAndUpdatedByAndUpdatedTsAfter(String status, String updatedBy, LocalDateTime startOfDay, Pageable pageable);

    Page<TbObApplicationMaster> findAll(Specification<TbObApplicationMaster> spec, Pageable pageable);

    Page<TbObApplicationMaster>findByGroupIdIn(List<String> groupIds, Pageable pageable);

}