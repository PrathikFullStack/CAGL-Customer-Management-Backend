package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmDocumentEntity;

@Repository
public interface CmDocumentRepository extends JpaRepository<CmDocumentEntity, String> {

    List<CmDocumentEntity> findByCustomerId(String customerId);

    List<CmDocumentEntity> findByApplicationId(String applicationId);

    Optional<CmDocumentEntity> findByCustomerIdAndKycType(String customerId, String kycType);

    Optional<CmDocumentEntity> findByCustomerIdAndCategory(String customerId, String category);
}
