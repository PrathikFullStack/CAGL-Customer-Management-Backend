package com.iexceed.appzillonbanking.cagl.repository.cus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.entity.GkKendraData;


@Repository
public interface GkKendraDataRepository extends JpaRepository<GkKendraData, Integer> {

	List<GkKendraData> findByBranchIdIn(List<String> branchIds);

	List<GkKendraData> findByBranchIdInAndKendraStatus(List<String> branchIds, String kendraStatus);

	List<GkKendraData> findByBranchIdInAndKendraIdInAndKendraStatus(List<String> branchIds, List<Integer> kendraIds,
			String kendraStatus);

}
