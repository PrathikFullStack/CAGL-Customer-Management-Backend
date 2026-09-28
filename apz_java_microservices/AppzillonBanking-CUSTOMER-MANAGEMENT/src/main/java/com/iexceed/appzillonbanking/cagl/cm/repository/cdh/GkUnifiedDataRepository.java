package com.iexceed.appzillonbanking.cagl.cm.repository.cdh;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.cdh.GkUnifiedDataEntity;

@Repository
public interface GkUnifiedDataRepository extends JpaRepository<GkUnifiedDataEntity, Integer> {

    Optional<GkUnifiedDataEntity> findByCustomerId(String customerId);

    List<GkUnifiedDataEntity> findByMobileNumber(String mobileNumber);

    Optional<GkUnifiedDataEntity> findByPrimaryTypeAndPrimaryId(String primaryType, String primaryId);

    Optional<GkUnifiedDataEntity> findByPanNumber(String panNumber);

    List<GkUnifiedDataEntity> findByKendraId(Integer kendraId);

    List<GkUnifiedDataEntity> findByBranchId(String branchId);

    @Query("SELECT g FROM GkUnifiedDataEntity g WHERE g.customerName LIKE %:keyword% OR g.mobileNumber LIKE %:keyword% OR g.customerId LIKE %:keyword% OR g.primaryId LIKE %:keyword%")
    List<GkUnifiedDataEntity> searchByKeyword(@Param("keyword") String keyword);
}
