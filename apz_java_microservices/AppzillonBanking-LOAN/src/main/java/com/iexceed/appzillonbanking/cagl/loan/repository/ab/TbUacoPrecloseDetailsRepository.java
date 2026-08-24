package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import com.iexceed.appzillonbanking.cagl.loan.domain.ab.PreclosureDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbUacoPrecloseDetailsRepository extends JpaRepository<PreclosureDetails, String> {

    Optional<PreclosureDetails> findByApplicationId(String applicationId);
}
