package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface TbObGroupRepository extends JpaRepository<TbObGroup, String> {

    Optional<TbObGroup> findByGroupId(String groupId);
    Long countByStatusAndCreatedBy(String status, String createdBy);
    @Query("SELECT count(g) FROM TbObGroup g WHERE g.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId = :branchId) AND g.status = :status")
    long countByStatusAndBranchId(@Param("status") String status, @Param("branchId") String branchId);
    List<TbObGroup> findByStatusAndTotalMemberCountAndCreatedTsBefore(String status, String memberCount, LocalDateTime createdTsCutoff);
    long countByKendraId(String kendraId);
    Optional<TbObGroup> findByGroupNameAndKendraId(String groupName, String kendraId);
    long countByStatusAndCreatedByAndKendraIdIn(String active, String userId, List<String> kendraIds);
}
