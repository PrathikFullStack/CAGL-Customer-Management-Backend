package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGRT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObGRTRepository extends JpaRepository<TbObGRT, String> {
    Optional<TbObGRT> findByGroupId(String groupId);

    @Query("SELECT d.groupId FROM TbObGRT d WHERE d.status = :status")
    List<String> findGroupIdsByStatus(@Param("status") String status);
}
