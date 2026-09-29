package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;

@Repository
public interface CmCustomerRepository extends JpaRepository<CmCustomerEntity, String> {

    Optional<CmCustomerEntity> findByCustomerId(String customerId);

    Optional<CmCustomerEntity> findByPrimaryKycTypeAndPrimaryKycId(String primaryKycType, String primaryKycId);

    @Query("SELECT c FROM CmCustomerEntity c WHERE c.customerId IN :customerIds")
    List<CmCustomerEntity> findByCustomerIds(@Param("customerIds") List<String> customerIds);
}
