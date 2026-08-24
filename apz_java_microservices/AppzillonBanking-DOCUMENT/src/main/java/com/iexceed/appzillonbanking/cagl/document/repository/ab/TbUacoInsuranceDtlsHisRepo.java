package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.TbUacoInsuranceDtlsHis;

@Repository
public interface TbUacoInsuranceDtlsHisRepo extends CrudRepository<TbUacoInsuranceDtlsHis, String> {

    Optional<TbUacoInsuranceDtlsHis> findByApplicationId(String applicationId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE TB_UACO_INSURANCE_DETAILS_HISTORY SET  payload =:payload WHERE APPLICATION_ID =:applicationId", nativeQuery = true)
    int updateInsuranceValuesPostRetrigger(@Param("payload") String payload,
                                           @Param("applicationId") String applicationId);

}
