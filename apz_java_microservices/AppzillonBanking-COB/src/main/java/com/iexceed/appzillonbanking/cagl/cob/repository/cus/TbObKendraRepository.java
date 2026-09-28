package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TbObKendraRepository extends JpaRepository<TbObKendra, String>, JpaSpecificationExecutor<TbObKendra> {
    Optional<TbObKendra> findByKendraNameAndBranchId(String kendraName, String branchId);
    Optional<TbObKendra> findByKendraId(String kendraId);
    long countByStatusAndBranchId(String status, String branchId);
    long countByStatusAndBranchIdIn(String status, List<String> branchIds);
    long countByStatusAndKendraIdIn(String status, List<String> kendraIds);
    List<TbObKendra> findByStatusInAndCreatedTsBefore(List<String> statuses, LocalDateTime cutoff);

    List<TbObKendra> findByKmId(String kmId);

    List<TbObKendra> findByBranchId(String branchId);

    @Query(value = "SELECT nextval('seq_ob_kendra_id')", nativeQuery = true)
    Long getNextKendraId();

    Optional<TbObKendra> findByKendraName(String kendraName);

    @Query("SELECT COUNT(k) FROM TbObKendra k WHERE k.status = 'ACTIVE' AND k.t24RefNo IS NULL")
    long countPendingForT24Activation();


}
