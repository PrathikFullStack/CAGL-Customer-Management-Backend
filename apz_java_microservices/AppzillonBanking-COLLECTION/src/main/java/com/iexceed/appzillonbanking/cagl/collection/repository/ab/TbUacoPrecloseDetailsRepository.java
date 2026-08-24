package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.PreclosureDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface TbUacoPrecloseDetailsRepository extends JpaRepository<PreclosureDtls, Long> {

    List<PreclosureDtls> findByBranchCodeAndMeetingDate(String branchCode, LocalDate meetingDate);

    List<PreclosureDtls> findByCustomerIdAndMeetingDate(String customerId, LocalDate meetingDate);

    Optional<PreclosureDtls> findByApplicationIdAndMeetingDate(String applicationId, LocalDate meetingDate);
}
