package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUaobCustDtls;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUaobCustDtlsId;

public interface TbUaobCustomerDtlsRepo extends CrudRepository<TbUaobCustDtls, TbUaobCustDtlsId> {

	Optional<TbUaobCustDtls> findByApplicationId(String applicationId);

	Optional<TbUaobCustDtls> findByApplicationIdOrderByVersionNoDesc(String applicationId);

	List<TbUaobCustDtls> findByAppIdAndApplicationIdOrderByVersionNoDesc(String appId, String applnId);
	
//	List<TbUaobCustDtls> findByAppIdAndApplicationIdAndKendraIdAndVersionNo(String appId, String applnId,
//			String kendraId, String versionNo);

//	@Query(
//			value = """
//        SELECT t.*
//        FROM tb_uaob_customer_details t
//        WHERE t.app_id = :appId
//          AND t.application_id = :applicationId
//          AND t.kendra_id = :kendraId
//          AND t.version_no = :versionNo
//        ORDER BY (
//                    SELECT MIN(CAST(m ->> 'pos' AS integer))
//                    FROM jsonb_array_elements(CAST(t.payload AS jsonb) -> 'members') AS m
//                )
//        """,
//			nativeQuery = true
//	)
//	List<TbUaobCustDtls> findByAppAndApplicationAndKendraOrderedByMemberPos(
//			@Param("appId") String appId,
//			@Param("applicationId") String applicationId,
//			@Param("kendraId") String kendraId,
//			@Param("versionNo") String versionNo
//	);

	@Query(
			value = """
    SELECT t.*
    FROM tb_uaob_customer_details t
    CROSS JOIN LATERAL (
        SELECT MIN(CAST(m ->> 'pos' AS integer)) AS min_pos
        FROM jsonb_array_elements(
            COALESCE(CAST(t.payload AS jsonb) -> 'members', CAST('[]' AS jsonb))
        ) AS m
    ) mp
    WHERE t.app_id = :appId
      AND t.application_id = :applicationId
      AND t.kendra_id = :kendraId
      AND t.version_no = :versionNo
    ORDER BY mp.min_pos
    """,
			nativeQuery = true
	)
	List<TbUaobCustDtls> findByAppAndApplicationAndKendraOrderedByMemberPos(
			@Param("appId") String appId,
			@Param("applicationId") String applicationId,
			@Param("kendraId") String kendraId,
			@Param("versionNo") String versionNo
	);
	
	List<TbUaobCustDtls> findByAppIdAndApplicationIdAndVersionNo(String appId, String applnId,
			String versionNo);
	
	Optional<TbUaobCustDtls> findByCustomerId(String memberId);

	@Query("SELECT t FROM TbUaobCustDtls t WHERE t.appId IN :appIds AND t.applicationId IN :applicationIds")
	List<TbUaobCustDtls> findAllByAppIdsAndApplicationIds(@Param("appIds")List<String> appIds, @Param("applicationIds")List<String> applicationIds);
}
