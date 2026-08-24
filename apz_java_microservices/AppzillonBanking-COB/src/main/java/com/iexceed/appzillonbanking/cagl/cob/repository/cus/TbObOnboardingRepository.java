package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for TbObApplicationMaster.
 * Dashboard queries optimized for role-based filtering.
 * All data from single table: tb_ob_application_master
 */
@Repository
public interface TbObOnboardingRepository extends JpaRepository<TbObApplicationMaster, String> {
    long countByWfStage(String wfStage);

    // Fetching lists for tiles based on status and kmId
    Page<TbObApplicationMaster> findByStatusAndCreatedBy(String status, String createdBy, Pageable pageable);

    // For filtering with case aging
    Page<TbObApplicationMaster> findByStatusAndCreatedByAndCreatedTsBefore(String status, String createdBy, LocalDateTime beforeDate, Pageable pageable);

    // For filtering with case aging on multiple statuses
    Page<TbObApplicationMaster> findByStatusInAndCreatedByAndCreatedTsBefore(List<String> statuses, String createdBy, LocalDateTime beforeDate, Pageable pageable);

    // Fetching list for 'All' sub-category in Draft & CB_FAIL tiles
    Page<TbObApplicationMaster> findByStatusInAndCreatedBy(List<String> statuses, String createdBy, Pageable pageable);

    // Fetching lists for tiles based on stage (general pool)
    Page<TbObApplicationMaster> findByStage(String stage, Pageable pageable);

    // Fetching list for Green Submitted tile
    Page<TbObApplicationMaster> findByStatusAndChannelTypeAndCreatedBy(String status, String channelType, String createdBy, Pageable pageable);

    // Fetching lists for tiles based on stage and kmId
    Page<TbObApplicationMaster> findByStageAndCreatedBy(String stage, String createdBy, Pageable pageable);

    // Fetching lists for tiles based on record type and kmId
    Page<TbObApplicationMaster> findByRecordTypeAndCreatedBy(String recordType, String createdBy, Pageable pageable);

    // Fetching list for 'All' sub-category in InputLoanDetails tile
    Page<TbObApplicationMaster> findByRecordTypeInAndCreatedBy(List<String> recordTypes, String createdBy, Pageable pageable);

    // Fetching lists for Reinterview tile
    Page<TbObApplicationMaster> findByStageAndRecordTypeAndCreatedBy(String stage, String recordType, String createdBy, Pageable pageable);

    // Fetching list for 'All' sub-category in Reinterview tile
    Page<TbObApplicationMaster> findByStageAndRecordTypeInAndCreatedBy(String stage, List<String> recordTypes, String createdBy, Pageable pageable);

    // Branch Manager (BM) specific queries
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

    // Fetching list for Activated/Rejected tiles for the current day
    @Query("SELECT app FROM TbObApplicationMaster app WHERE app.status IN :statuses AND app.createdBy = :userId AND app.updatedTs >= current_date")
    Page<TbObApplicationMaster> findByStatusInAndCreatedByForCurrentDay(@Param("statuses") List<String> statuses, @Param("userId") String userId, Pageable pageable);

    // Fetching list for Pending for Activation tile
    Page<TbObApplicationMaster> findByStatusAndCreatedByAndLoanIdIsNull(String status, String createdBy, Pageable pageable);

    // Fetching list for CRT Pending Queue based on workflow status and Kendra IDs
    Page<TbObApplicationMaster> findByStatusAndKendraIdIn(String status, List<Long> kendraIds, Pageable pageable);

    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.createdBy = :userId " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy")
    List<DashboardCountDTO> getCountsByKmIdGrouped(@Param("userId") String userId);

    @Query("SELECT new com.iexceed.appzillonbanking.cagl.cob.payload.DashboardCountDTO(a.status, a.stage, a.recordType, a.channelType, a.createdBy, count(a.id)) " +
            "FROM TbObApplicationMaster a " +
            "WHERE a.branchId = :branchId " +
            "GROUP BY a.status, a.stage, a.recordType, a.channelType, a.createdBy")
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

    // Global Search Queries
    Page<TbObApplicationMaster> findByMobileNumberContainingIgnoreCase(String mobileNo, Pageable pageable);
    Page<TbObApplicationMaster> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    Page<TbObApplicationMaster> findByApplicationIdContainingIgnoreCase(String applicationId, Pageable pageable);
    Page<TbObApplicationMaster> findByKendraId(Long kendraId, Pageable pageable);
    Page<TbObApplicationMaster> findByKendraNameContainingIgnoreCase(String kendraName, Pageable pageable);
    Page<TbObApplicationMaster> findByGroupId(Long groupId, Pageable pageable);
    Page<TbObApplicationMaster> findByLeadIdContainingIgnoreCase(String leadId, Pageable pageable);

    // Global Search Queries with KM data scoping
    Page<TbObApplicationMaster> findByMobileNumberContainingIgnoreCaseAndCreatedBy(String mobileNo, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByCustomerNameContainingIgnoreCaseAndCreatedBy(String customerName, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByApplicationIdContainingIgnoreCaseAndCreatedBy(String applicationId, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByKendraIdAndCreatedBy(Long kendraId, String createdBy, Pageable pageable);
    Page<TbObApplicationMaster> findByGroupIdAndCreatedBy(Long groupId, String createdBy, Pageable pageable);

}
