package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObBMReInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbObBMReInterviewRepository extends JpaRepository<TbObBMReInterview, Long> {
    Optional<TbObBMReInterview> findByApplicationId(String applicationId);
}