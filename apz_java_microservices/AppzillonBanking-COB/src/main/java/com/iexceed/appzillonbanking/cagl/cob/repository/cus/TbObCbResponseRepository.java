package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCbResponse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbObCbResponseRepository extends CrudRepository<TbObCbResponse, Long> {
    Optional<TbObCbResponse> findByApplicationId(String applicationId);
}