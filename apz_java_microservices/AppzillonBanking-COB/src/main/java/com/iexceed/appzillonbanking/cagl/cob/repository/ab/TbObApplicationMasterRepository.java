package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObApplicationMasterRepository
        extends JpaRepository<TbObApplicationMaster, String> {

    Optional<TbObApplicationMaster> findByApplicationId(String applicationId);

    Optional<TbObApplicationMaster> findByCustomerId(String customerId);

    List<TbObApplicationMaster> findByCustomerIdIn(List<String> customerIds);

    @Query(value = "select * from tb_ob_application_master a WHERE a.status IN(:status) AND  a.application_id =:application_id", nativeQuery = true)
    List<TbObApplicationMaster> findApplicationBasedOnApplicationId(@Param("application_id") String applicationId,
                                                                    @Param("status") List<String> statuses);

    List<TbObApplicationMaster> findByGroupIdAndStatusNot(String groupId, String rejected);

    Optional<TbObApplicationMaster> findByMobileNumber(String mobileNumber);

    List<TbObApplicationMaster> findByGroupIdAndStatus(String groupId, String status);
}