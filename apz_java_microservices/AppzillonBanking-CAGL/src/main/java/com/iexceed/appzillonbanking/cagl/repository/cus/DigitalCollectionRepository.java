package com.iexceed.appzillonbanking.cagl.repository.cus;

import com.iexceed.appzillonbanking.cagl.entity.DigitalCollection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DigitalCollectionRepository extends JpaRepository<DigitalCollection, Long> {

	@Query("""
			    select dc.amount
			    from DigitalCollection dc
			    where dc.customerId = :customerId
			      and dc.trnPostedAt >= :startDateTime
			      and dc.trnPostedAt < :endDateTime
			""")
	Double findMahiAmountByCustomerIdAndDateRange(@Param("customerId") String customerId,
			@Param("startDateTime") String startDateTime, @Param("endDateTime") String endDateTime);

	List<DigitalCollection> findByKendraIdInAndTrnPostedAtGreaterThanEqualAndTrnPostedAtLessThan(List<String> kendraId,
			String startDateTime, String endDateTime);

	List<DigitalCollection> findByBranchIdAndTrnPostedAtGreaterThanEqualAndTrnPostedAtLessThan(String branchId,
			String startDateTime, String endDateTime);

	List<DigitalCollection> findByCustomerIdAndTrnPostedAtGreaterThanEqualAndTrnPostedAtLessThan(
			String customerId, String startDateTime, String endDateTime);

	List<DigitalCollection> findByGroupIdAndTrnPostedAtGreaterThanEqualAndTrnPostedAtLessThan(
			String groupId, String startDateTime, String endDateTime);
}
