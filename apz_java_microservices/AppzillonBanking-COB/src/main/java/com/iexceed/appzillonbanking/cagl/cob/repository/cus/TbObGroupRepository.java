package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface TbObGroupRepository extends JpaRepository<TbObGroup, String>, JpaSpecificationExecutor<TbObGroup> {

    Optional<TbObGroup> findByGroupId(String groupId);
    @Query("SELECT count(g) FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId = :branchId) AND g.status = :status")

    long countByStatusAndBranchId(@Param("status") String status, @Param("branchId") String branchId);

    @Query("SELECT count(g) FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId IN :branchIds) AND g.status = :status")
    long countByStatusAndBranchIdIn(@Param("status") String status, @Param("branchIds") List<String> branchIds);

    List<TbObGroup> findByStatusAndTotalMemberCountAndCreatedTsBefore(String status, String memberCount, LocalDateTime createdTsCutoff);

    long countByKendraId(String kendraId);

    Optional<TbObGroup> findByGroupNameAndKendraId(String groupName, String kendraId);

    List<TbObGroup> findByKendraId(String kendraId);

    @Query(value = "SELECT nextval('seq_ob_group_id')", nativeQuery = true)
    Long getNextGroupId();

    List<TbObGroup> findByKendraIdIn(List<String> kendraIds);
    @Query("SELECT COUNT(g) FROM TbObGroup g WHERE g.status = 'ACTIVE' AND g.t24RefNo IS NULL")
    long countPendingForT24Activation();

    // CGT tile: groups still pending CGT, scoped to the caller's kendras (KM) / branches (BM, AM).
    List<TbObGroup> findByKendraIdInAndCgtStatus(List<String> kendraIds, String cgtStatus);

    // PENDING_FOR_CGT tile (KM): scoped directly by the groupIds the caller passes, not kendraIds.
    List<TbObGroup> findByGroupIdInAndCgtStatus(List<String> groupIds, String cgtStatus);

    @Query("SELECT g FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId IN :branchIds) AND g.cgtStatus = :cgtStatus")
    List<TbObGroup> findByBranchIdInAndCgtStatus(@Param("branchIds") List<String> branchIds, @Param("cgtStatus") String cgtStatus);

    @Query("SELECT g FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId = :branchId) AND g.cgtStatus = :cgtStatus")
    List<TbObGroup> findByBranchIdAndCgtStatus(@Param("branchId") String branchId, @Param("cgtStatus") String cgtStatus);

    // PENDING_FOR_GRT tile: groups still pending GRT, scoped to the caller's kendras (KM) / branches (BM, AM).
    List<TbObGroup> findByKendraIdInAndGrtStatus(List<String> kendraIds, String grtStatus);

    @Query("SELECT g FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId IN :branchIds) AND g.grtStatus = :grtStatus")
    List<TbObGroup> findByBranchIdInAndGrtStatus(@Param("branchIds") List<String> branchIds, @Param("grtStatus") String grtStatus);

    @Query("SELECT g FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId = :branchId) AND g.grtStatus = :grtStatus")
    List<TbObGroup> findByBranchIdAndGrtStatus(@Param("branchId") String branchId, @Param("grtStatus") String grtStatus);


    long countByStatusAndKendraIdIn(String status, List<String> kendraIds);
}
