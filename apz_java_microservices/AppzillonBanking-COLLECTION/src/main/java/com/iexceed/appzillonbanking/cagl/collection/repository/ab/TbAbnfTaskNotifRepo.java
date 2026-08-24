package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbAbnfTaskNotif;

@Repository
public interface TbAbnfTaskNotifRepo extends JpaRepository<TbAbnfTaskNotif, Long> {

	List<TbAbnfTaskNotif> findByBranchIdAndFromUserIdAndKendraIdAndApplicationDate(String branchId, String fromUserId,
			String kendraId, LocalDate applicationDate);

	List<TbAbnfTaskNotif> findAllByBranchIdAndFromUserIdAndKendraId(String branchId, String fromUserId,
			String kendraId);

	List<TbAbnfTaskNotif> findByBranchIdAndToUserIdAndFromUserRoleAndKendraIdAndApplicationDate(String branchId,
			String toUserId, String fromUserRole, String kendraId, LocalDate applicationDate);

	List<TbAbnfTaskNotif> findByBranchIdAndKendraIdAndApplicationDateAndToUserRole(String branchId, String kendraId,
			LocalDate applicationDate, String toUserRole);

}
