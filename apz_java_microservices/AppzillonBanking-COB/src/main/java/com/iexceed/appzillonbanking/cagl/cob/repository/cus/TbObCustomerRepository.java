package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbObCustomerRepository extends JpaRepository<TbObCustomer, String> {
    Optional<TbObCustomer> findByApplicationId(String applicationId);
    Optional<TbObCustomer> findByCustomerId(String customerId);
}