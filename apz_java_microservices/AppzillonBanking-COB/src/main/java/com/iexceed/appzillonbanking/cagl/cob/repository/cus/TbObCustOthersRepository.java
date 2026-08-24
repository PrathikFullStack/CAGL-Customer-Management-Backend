package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustOthers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbObCustOthersRepository extends JpaRepository<TbObCustOthers, String> {
    List<TbObCustOthers> findByCustomerIdIn(List<String> customerIds);

    Optional<TbObCustOthers> findByApplicationId(String applicationId);
}