package com.iexceed.appzillonbanking.cagl.cob.repository.ab;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    long countByMobileNumber(String mobileNumber);

    List<TbObApplicationMaster> findByGroupIdAndStatus(String groupId, String status);

    // Bulk lookup backing the photo-dedupe status update -- resolves every requested
    // application_id in a single round trip so the batch can be validated/updated at once.
    List<TbObApplicationMaster> findByApplicationIdIn(List<String> applicationIds);

    // Paginated backlog of applications still awaiting a photo-dedupe outcome.
    Page<TbObApplicationMaster> findByPhotoDedupeStatus(String photoDedupeStatus, Pageable pageable);

    List<TbObApplicationMaster> findByDmsFolderIdxIsNull();
    List<TbObApplicationMaster> findByDmsUploadFlag(String dmsUploadFlag);
}