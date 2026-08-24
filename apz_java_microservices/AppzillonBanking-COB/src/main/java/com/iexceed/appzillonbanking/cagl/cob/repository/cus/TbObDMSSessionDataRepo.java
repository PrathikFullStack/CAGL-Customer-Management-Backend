package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDMSSessionDataEntity;

@Repository
public interface TbObDMSSessionDataRepo extends JpaRepository<TbObDMSSessionDataEntity, Date> {

    @Query(value =
            "SELECT * " +
                    "FROM tb_ob_dms_session_data " +
                    "WHERE DATE(date) = DATE(:date)",
            nativeQuery = true)
    Optional<TbObDMSSessionDataEntity> getSessionData(
            @Param("date") Date date);

}