package com.iexceed.appzillonbanking.scheduler.repository.ab;

import com.iexceed.appzillonbanking.scheduler.domain.ab.TbObDmsFailedUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObDmsFailedUploadRepo extends JpaRepository<TbObDmsFailedUpload, Long> {
    List<TbObDmsFailedUpload> findByApplicationId(String applicationId);
    Optional<TbObDmsFailedUpload> findByApplicationIdAndDocumentId(String applicationId, String documentId);
}
