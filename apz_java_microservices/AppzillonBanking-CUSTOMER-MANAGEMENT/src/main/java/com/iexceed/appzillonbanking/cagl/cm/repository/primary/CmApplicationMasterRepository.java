package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;

@Repository
public interface CmApplicationMasterRepository extends JpaRepository<CmApplicationMasterEntity, String> {

    Optional<CmApplicationMasterEntity> findByApplicationId(String applicationId);

    Optional<CmApplicationMasterEntity> findByCustomerId(String customerId);

    List<CmApplicationMasterEntity> findByKendraId(String kendraId);

    List<CmApplicationMasterEntity> findByBranchId(String branchId);

    List<CmApplicationMasterEntity> findByStageAndStatus(String stage, String status);

    @Query("SELECT a FROM CmApplicationMasterEntity a WHERE a.customerName LIKE %:search% OR a.mobileNumber LIKE %:search% OR a.customerId LIKE %:search%")
    List<CmApplicationMasterEntity> searchByKeyword(@Param("search") String search);
}
