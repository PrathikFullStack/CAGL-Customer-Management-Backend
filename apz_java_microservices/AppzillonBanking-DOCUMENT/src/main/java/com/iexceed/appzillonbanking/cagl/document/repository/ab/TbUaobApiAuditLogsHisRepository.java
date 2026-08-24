package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.TbUaobApiAuditLogsHis;
import com.iexceed.appzillonbanking.cagl.document.payload.PrecloserResponse;

@Repository
public interface TbUaobApiAuditLogsHisRepository extends JpaRepository<TbUaobApiAuditLogsHis, String> {

	@Query(value = "SELECT * FROM TB_UAOB_API_AUDIT_LOGS_HISTORY WHERE APPLICATION_ID =:applicationId AND API_STATUS ='SUCCESS' AND API_NAME ='preClosureLoan'", nativeQuery = true)
	TbUaobApiAuditLogsHis findApplicationId(@Param("applicationId") String applicationId);

	@Query(value = "SELECT * FROM TB_UAOB_API_AUDIT_LOGS_HISTORY WHERE APPLICATION_ID =:applicationId AND API_STATUS ='SUCCESS' AND API_NAME ='preClosureLoan'", nativeQuery = true)
	TbUaobApiAuditLogsHis findApplicationIdForReport(@Param("applicationId") String applicationId);

	@Query(value = "SELECT " + "u.REQUEST_PAYLOAD AS requestPayload, " + "u.RESPONSE_PAYLOAD AS responsePayload, "
			+ "u.API_REQ_TS AS apiReqTs, " + "u.API_RES_TS AS apiResTs " + "FROM TB_UAOB_API_AUDIT_LOGS_HISTORY u "
			+ "WHERE u.APPLICATION_ID =:applicationId " + "AND u.API_NAME ='preClosureLoan' "
			+ "ORDER BY u.API_RES_TS DESC LIMIT 1", nativeQuery = true)
	PrecloserResponse findByApplicationidApiName(@Param("applicationId") String applicationId);

	List<TbUaobApiAuditLogsHis> findTop100BySchedulerStatusOrderByReqTsDesc(String schedulerStatus);

	List<TbUaobApiAuditLogsHis> findByApplicationIdAndCustDtlId(String applicationId, String custDtlId);
}
