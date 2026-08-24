package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<TbObCustomer, Long> {

}