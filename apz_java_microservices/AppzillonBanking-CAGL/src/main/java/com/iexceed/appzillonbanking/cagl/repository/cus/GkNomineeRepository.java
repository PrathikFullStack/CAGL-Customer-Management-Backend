package com.iexceed.appzillonbanking.cagl.repository.cus;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.entity.GkNominee;
import com.iexceed.appzillonbanking.cagl.entity.GkNomineeId;

@Repository
public interface GkNomineeRepository extends JpaRepository<GkNominee, GkNomineeId> {

	List<GkNominee> findByCustid(String custid);

	@Query("SELECT gkN FROM GkNominee gkN where gkN.custid IN (:CustId)")
	List<GkNominee> fetchGkNomineeByCustId(@Param("CustId") List<String> CustId);
}
