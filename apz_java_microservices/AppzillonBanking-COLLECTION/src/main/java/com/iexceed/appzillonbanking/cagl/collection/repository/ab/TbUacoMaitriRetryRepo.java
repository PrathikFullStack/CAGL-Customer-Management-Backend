package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoMaitriRetry;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoMaitriRetryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TbUacoMaitriRetryRepo extends JpaRepository<TbUacoMaitriRetry, TbUacoMaitriRetryId> {

    List<TbUacoMaitriRetry> findBySchedulerStatus(String schedulerStatus);
}
