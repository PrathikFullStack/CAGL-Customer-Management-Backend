package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObRecordLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbObRecordLockRepo extends JpaRepository<TbObRecordLockEntity, Long> {

    Optional<TbObRecordLockEntity> findByApplicationId(String applicationId);

    @Query(""" 
         SELECT r FROM TbObRecordLockEntity r WHERE r.applicationId = :applicationId AND r.status = 'ACTIVE'""")
    Optional<TbObRecordLockEntity> findActiveByApplicationId(@Param("applicationId") String applicationId);
}