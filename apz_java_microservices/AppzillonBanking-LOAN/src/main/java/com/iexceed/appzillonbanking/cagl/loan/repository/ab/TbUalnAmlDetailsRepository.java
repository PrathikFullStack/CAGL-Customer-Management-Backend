package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import com.iexceed.appzillonbanking.cagl.loan.domain.ab.TbUalnAmlDetails;
import org.springframework.data.jpa.repository.JpaRepository; import
org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository public interface TbUalnAmlDetailsRepository extends JpaRepository<TbUalnAmlDetails, String> {
    Optional<TbUalnAmlDetails> findByApplicationId(String applicationId);
}
