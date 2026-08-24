package com.iexceed.appzillonbanking.cagl.loan.bulkupload.repository.ab;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.bulkupload.domain.ab.BulkRecordsEntity;

@Repository
public interface BulkRecordsRepo extends CrudRepository<BulkRecordsEntity, Long> {

	Optional<List<BulkRecordsEntity>> findByDocId(@Param("docId") String docId);

}
