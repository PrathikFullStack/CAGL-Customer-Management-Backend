package com.iexceed.appzillonbanking.scheduler.repository.ab;

import com.iexceed.appzillonbanking.scheduler.domain.ab.TbObGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TbObGroupRepository extends JpaRepository<TbObGroup, Long> {

    List<TbObGroup> findByStatusAndMemberCountAndCreatedTsBefore(
            String status, Integer memberCount, LocalDateTime cutoff);

    long countByKendraId(Long kendraId);
}
