package com.iexceed.appzillonbanking.cagl.cob.repository.cus;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocument;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbObDocumentRepository extends JpaRepository<TbObDocument, TbObDocumentId> {

    List<TbObDocument> findByApplicationIdOrderByDocuId(String applicationId);

    List<TbObDocument> findByApplicationId(String applicationId);

    Optional<TbObDocument> findByApplicationIdAndLegalDocName(String applicationId, String legalDocName);
}
