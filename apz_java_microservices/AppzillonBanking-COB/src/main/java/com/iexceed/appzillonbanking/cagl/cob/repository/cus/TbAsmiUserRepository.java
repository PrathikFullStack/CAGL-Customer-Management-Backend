package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbAsmiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TbAsmiUserRepository extends JpaRepository<TbAsmiUser, TbAsmiUser.Id> {

    @Query("SELECT u FROM TbAsmiUser u " +
            "WHERE u.userPhno1 = :mobile OR u.userPhno2 = :mobile")
    Optional<TbAsmiUser> findByMobileNumber(@Param("mobile") String mobile);

    @Query("""
    SELECT COUNT(u)
    FROM TbAsmiUser u
    WHERE u.userPhno1 = :mobile
       OR u.userPhno2 = :mobile
    """)
    long countByMobileNumber(@Param("mobile") String mobile);
}
