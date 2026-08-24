package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObOtherDocument;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObOtherDocumentPK;
import java.util.List;

@Repository
public interface TbObOtherDocumentRepository extends JpaRepository<TbObOtherDocument, TbObOtherDocumentPK> {
	@Query("SELECT d FROM TbObOtherDocument d WHERE d.id = :id")
	List<TbObOtherDocument> findByOtherId(@Param("id") String id);

    List<TbObOtherDocument> findByApplicationIdOrderByDocuIdAsc(String applicationId);
}
