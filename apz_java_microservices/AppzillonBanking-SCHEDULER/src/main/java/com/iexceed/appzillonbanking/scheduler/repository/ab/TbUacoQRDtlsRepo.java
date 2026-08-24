package com.iexceed.appzillonbanking.scheduler.repository.ab;

import com.iexceed.appzillonbanking.scheduler.domain.ab.TbUacoQRDtls;
import com.iexceed.appzillonbanking.scheduler.domain.ab.TbUacoQRDtlsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TbUacoQRDtlsRepo extends JpaRepository<TbUacoQRDtls, TbUacoQRDtlsId> {

    Optional<TbUacoQRDtls> findByBillNumber(String billNumber);

}
