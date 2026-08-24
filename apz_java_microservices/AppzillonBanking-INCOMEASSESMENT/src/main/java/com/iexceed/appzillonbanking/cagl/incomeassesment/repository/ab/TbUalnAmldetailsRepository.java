package com.iexceed.appzillonbanking.cagl.incomeassesment.repository.ab;

import com.iexceed.appzillonbanking.cagl.incomeassesment.domain.ab.TbUalnAmldetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbUalnAmldetailsRepository extends JpaRepository<TbUalnAmldetails, String> {
    Optional<TbUalnAmldetails> findByApplicationId(String applicationId);
}
