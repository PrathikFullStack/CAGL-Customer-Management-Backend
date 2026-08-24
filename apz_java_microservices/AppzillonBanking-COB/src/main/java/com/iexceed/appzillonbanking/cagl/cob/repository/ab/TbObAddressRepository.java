package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObAddress;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObAddressRepository extends JpaRepository<TbObAddress, TbObApplicationMaster.TbObAddressId> {
    List<TbObAddress> findByApplicationIdOrderByAddressType(String applicationId);

    List<TbObAddress> findByApplicationId(String applicationId);

    Optional<TbObAddress> findByApplicationIdAndAddressType(String applicationId, String addressType);
}
