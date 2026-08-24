package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.TbUaobCbResponseHis;

public interface TbUaobCbResponseHisRepository extends CrudRepository<TbUaobCbResponseHis, String> {

    Optional<TbUaobCbResponseHis> findByAppIdAndApplicationIdOrderByVersionNumDesc(String appId, String applicationId);

    Optional<TbUaobCbResponseHis> findTopByApplicationIdOrderByResTsDesc(String applicationId);

    @Query(value = "SELECT * FROM TB_UAOB_CB_RESPONSE_HISTORY WHERE APPLICATION_ID =:applicationId ORDER BY REQ_TS DESC LIMIT 1", nativeQuery = true)
    TbUaobCbResponseHis findByAppIdAndApplicationId(@Param("applicationId") String applicationId);

    Optional<TbUaobCbResponseHis> findByApplicationIdOrderByVersionNumDesc(String applicationId);

    Optional<TbUaobCbResponseHis> findTopByApplicationIdOrderByVersionNumDesc(String applicationId);

    @Query(value = "SELECT am.application_id " + "FROM public.TB_UACO_APPLICATION_MASTER_HISTORY am "
            + "WHERE am.customer_id = :customerId and am.application_type is null " + "AND NOT EXISTS ( " + "SELECT 1 "
            + "FROM public.TB_UAOB_CB_RESPONSE_HISTORY cr "
            + "WHERE cr.application_id = am.application_id );", nativeQuery = true)
    List<String> getApplicationListAfterOTPDrop(String customerId);

}
