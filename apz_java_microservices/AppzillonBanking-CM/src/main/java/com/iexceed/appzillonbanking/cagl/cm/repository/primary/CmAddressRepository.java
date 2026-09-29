package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmAddressEntity;

@Repository
public interface CmAddressRepository extends JpaRepository<CmAddressEntity, String> {

    List<CmAddressEntity> findByCustomerId(String customerId);

    Optional<CmAddressEntity> findByCustomerIdAndAddressType(String customerId, String addressType);

    List<CmAddressEntity> findByApplicationId(String applicationId);
}
