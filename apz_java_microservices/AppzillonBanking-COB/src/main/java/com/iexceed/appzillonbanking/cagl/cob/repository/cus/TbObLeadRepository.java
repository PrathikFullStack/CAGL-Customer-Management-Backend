package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface TbObLeadRepository extends JpaRepository<TbObLead, String> {

    // ── Deduplication ─────────────────────────────────────────────────────

    @Query("SELECT l FROM TbObLead l " +
           "WHERE l.mobileNumber = :mobileNumber " +
           "AND l.status = :status")
    Optional<TbObLead> findDuplicate(@Param("mobileNumber") String mobileNumber,
                                     @Param("status") String status);

    long countByMobileNumberAndStatus(@Param("mobileNumber") String mobileNumber, @Param("status") String status);

    long countByCreatedBy(String createdBy);

    long countByBranchId(String branchId);

    long countByBranchIdIn(List<String> branchIds);

    Optional<TbObLead> findByLeadId(String leadId);

}
