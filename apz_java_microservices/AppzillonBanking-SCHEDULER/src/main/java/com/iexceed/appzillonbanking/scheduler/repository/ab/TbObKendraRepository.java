package com.iexceed.appzillonbanking.scheduler.repository.ab;

import com.iexceed.appzillonbanking.scheduler.domain.ab.TbObKendra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TbObKendraRepository extends JpaRepository<TbObKendra, Long> {

    List<TbObKendra> findByStatusInAndCreatedTsBefore(
            List<String> statuses, LocalDateTime cutoff);
}
