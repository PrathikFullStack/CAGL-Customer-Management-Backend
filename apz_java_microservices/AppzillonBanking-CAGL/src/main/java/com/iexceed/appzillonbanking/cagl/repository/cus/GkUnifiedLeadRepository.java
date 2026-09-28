package com.iexceed.appzillonbanking.cagl.repository.cus;


import com.iexceed.appzillonbanking.cagl.entity.GkUnifiedLeadData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GkUnifiedLeadRepository extends JpaRepository<GkUnifiedLeadData, Long> {

    long countByKendraIdIn(List<String> kendraIds);

    long countByBranchId(String branchId);
}
