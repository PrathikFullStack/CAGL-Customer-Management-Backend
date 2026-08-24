package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoQRDtls;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoQRDtlsId;
import org.springframework.data.repository.query.Param;

public interface TbUacoQRDtlsRepo extends CrudRepository<TbUacoQRDtls, TbUacoQRDtlsId> {

	Optional<TbUacoQRDtls> findByBillNumber(String billNumber);

	List<TbUacoQRDtls> findByCustomerIdIn(List<String> customerId);

	@Query("SELECT t FROM TbUacoQRDtls t WHERE t.customerId IN :customerIds AND t.status = :status")
	List<TbUacoQRDtls> findByCustomerIdInAndStatus(List<String> customerIds, String status);

	@Query("SELECT t FROM TbUacoQRDtls t WHERE t.kendraId IN :kendraIds " +
			"AND CAST(t.createTs AS date) = CAST(:meetingDate AS date)")
	List<TbUacoQRDtls> findByKendraIdsAndMeetingDate(@Param("kendraIds") List<String> kendraIds, @Param("meetingDate") String meetingDate);

	@Query("SELECT t FROM TbUacoQRDtls t WHERE t.branchId = :branchId " +
			"AND CAST(t.createTs AS date) = CAST(:meetingDate AS date)")
	List<TbUacoQRDtls> findByBranchIdAndMeetingDate(@Param("branchId") String branchId, @Param("meetingDate") String meetingDate);

	long countByCustomerIdAndCreateTsBetween(
			String customerId, Timestamp startDate, Timestamp endDate);
}
