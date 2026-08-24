package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TbObKendraRepository extends JpaRepository<TbObKendra, String> {
    Optional<TbObKendra> findByKendraNameAndBranchId(String kendraName, String branchId);
    Page<TbObKendra> findByStatusAndCreatedBy(String status, String createdBy, Pageable pageable);
    Page<TbObKendra> findByStatusInAndCreatedBy(List<String> statuses, String createdBy, Pageable pageable);
    Page<TbObKendra> findByStatusAndKendraIdIn(String status, List<String> kendraIds, Pageable pageable);
    Page<TbObKendra> findByStatusInAndKendraIdIn(List<String> statuses, List<String> kendraIds, Pageable pageable);
    Page<TbObKendra> findByStatusAndBranchId(String status, String branchId, Pageable pageable);
    Page<TbObKendra> findByStatusInAndBranchId(List<String> statuses, String branchId, Pageable pageable);

    Optional<TbObKendra> findByKendraId(String kendraId);
    List<TbObKendra> findByCreatedBy(String createdBy);


    @Query(value = """
        SELECT k.*
        FROM tb_ob_kendra k
        WHERE k.status = 'ACTIVE'
          AND k.kendra_id IN (:kendraIds)
          AND (
                SELECT COALESCE(SUM(CAST(NULLIF(g.total_member_count, '') AS INTEGER)), 0)
                FROM tb_ob_group g
                WHERE g.kendra_id = k.kendra_id
              ) < 30
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_ob_kendra k
        WHERE k.status = 'ACTIVE'
          AND k.kendra_id IN (:kendraIds)
          AND (
                SELECT COALESCE(SUM(CAST(NULLIF(g.total_member_count, '') AS INTEGER)), 0)
                FROM tb_ob_group g
                WHERE g.kendra_id = k.kendra_id
              ) < 30
        """,
            nativeQuery = true)
    Page<TbObKendra> findActiveKendrasWithCapacity(@Param("kendraIds") List<String> kendraIds, Pageable pageable);

    @Query(value = """
        SELECT k.*
        FROM tb_ob_kendra k
        WHERE k.status = 'ACTIVE'
          AND k.branch_id = :branchId
          AND (
                SELECT COALESCE(SUM(CAST(NULLIF(g.total_member_count, '') AS INTEGER)), 0)
                FROM tb_ob_group g
                WHERE g.kendra_id = k.kendra_id
              ) < 30
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_ob_kendra k
        WHERE k.status = 'ACTIVE'
          AND k.branch_id = :branchId
          AND (
                SELECT COALESCE(SUM(CAST(NULLIF(g.total_member_count, '') AS INTEGER)), 0)
                FROM tb_ob_group g
                WHERE g.kendra_id = k.kendra_id
              ) < 30
        """,
            nativeQuery = true)
    Page<TbObKendra> findActiveKendrasWithCapacityForBranch(@Param("branchId") String branchId, Pageable pageable);

    Long  countByStatusAndCreatedBy(String status, String createdBy);
    long countByStatusAndBranchId(String status, String branchId);
    long countByStatusAndCreatedByAndKendraIdIn(String status, String userId, List<String> kendraIds);


    List<TbObKendra> findByStatusInAndCreatedTsBefore(List<String> statuses, LocalDateTime cutoff);
}
