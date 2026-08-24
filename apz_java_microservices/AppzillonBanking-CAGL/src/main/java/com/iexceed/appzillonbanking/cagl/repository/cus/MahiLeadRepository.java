package com.iexceed.appzillonbanking.cagl.repository.cus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.entity.MahiLead;

import java.util.List;

@Repository
public interface MahiLeadRepository extends JpaRepository<MahiLead, String> {

	@Query("SELECT lead FROM MahiLead lead where lead.kendra IN (:kendraId) ORDER BY dateSubmitted DESC")
	List<MahiLead> fetchMahiLeadByKendra(@Param("kendraId") List<String> kendraId);

	//@Query(value = "SELECT count(*) FROM mahi_lead  where kendra=:kendraId",nativeQuery = true)
	//int fetchMahiLeadCount(@Param("kendraId") String kendraId);

//	@Query(value = "SELECT count(*) FROM mahi_lead  where kendra=:kendraId",nativeQuery = true)
//	int fetchMahiLeadCount(@Param("kendraId") String kendraId);

	@Query(value = """
			SELECT kendra, COUNT(*)
			FROM mahi_lead
			WHERE kendra IN (:kendraIds)
			GROUP BY kendra
			""", nativeQuery = true)
	List<Object[]> fetchMahiLeadCount(@Param("kendraIds") List<String> kendraIds);

	@Query(value = """
			SELECT kendra, COUNT(*)
			FROM mahi_lead
			WHERE kendra IN (:kendraIds) and lead_status='Pending'
			GROUP BY kendra
			""", nativeQuery = true)
	List<Object[]> fetchPendingLeadCount(@Param("kendraIds") List<String> kendraIds);

}
