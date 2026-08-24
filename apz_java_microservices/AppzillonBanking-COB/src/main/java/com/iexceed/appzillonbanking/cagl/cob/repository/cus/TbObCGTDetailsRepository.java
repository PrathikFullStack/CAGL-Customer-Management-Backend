package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCGTDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObCGTDetailsRepository extends JpaRepository<TbObCGTDetails, String> {
    Page<TbObCGTDetails> findByStatusAndCreatedBy(String status, String createdBy, Pageable pageable);
    Optional<TbObCGTDetails> findByGroupId(String groupId);
    @Query("SELECT d.groupId FROM TbObCGTDetails d WHERE d.status = :status AND d.createdBy = :createdBy")
    List<String> findGroupIdsByStatusAndCreatedBy(@Param("status") String status, @Param("createdBy") String createdBy);
    @Query("SELECT d.groupId FROM TbObCGTDetails d WHERE d.status = :status AND d.kendraId IN :kendraIds")
    List<String> findGroupIdsByStatusAndKendraIdIn(@Param("status") String status, @Param("kendraIds") List<String> kendraIds);
    @Query("SELECT d.groupId FROM TbObCGTDetails d WHERE d.status = :status AND d.kendraId IN (SELECT k.kendraId FROM TbObKendra k WHERE k.branchId = :branchId)")
    List<String> findGroupIdsByStatusAndBranchId(@Param("status") String status, @Param("branchId") String branchId);
}