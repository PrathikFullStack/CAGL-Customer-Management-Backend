package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.ApplicationMaster;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.ApplicationMasterId;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicationMasterRepository extends CrudRepository<ApplicationMaster, ApplicationMasterId> {

	Optional<ApplicationMaster> findTopByAppIdAndApplicationIdAndKendraIdAndBranchCodeOrderByVersionNumDesc(
			String appId, String applicationId, String kendraId, String branchCode);

	List<ApplicationMaster> findByBranchCodeAndApplicationDateAndApplicationTypeAndKmIdInOrderByVersionNumDesc(
			String branchCode, LocalDate applnDate, String applnType, List<String> kmIds);

	List<ApplicationMaster> findByBranchCodeAndApplicationTypeAndKmIdInOrderByVersionNumDesc(String branchCode,
			String applnType, List<String> kmIds);

	List<ApplicationMaster> findByAppIdAndApplicationIdAndVersionNumAndKendraIdInOrderByVersionNumDesc(String appId,
			String applicationId, String versionNo, List<String> kendraId);

	List<ApplicationMaster> findByAppIdAndVersionNumAndKendraIdInAndApplicationTypeNotOrderByVersionNumDesc(
			String appId, String versionNo, List<String> kendraId, String applnType);

	List<ApplicationMaster> findByAppIdAndVersionNumAndKendraIdInAndApplicationTypeNotAndApplicationStatusNotOrderByVersionNumDesc(
			String appId, String versionNo, List<String> kendraId, String applnType, String applnStatus);

	List<ApplicationMaster> findByAppIdAndApplicationIdAndVersionNumAndKendraIdInAndApplicationTypeNotAndApplicationStatusNotOrderByVersionNumDesc(
			String appId, String applicationId, String versionNo, List<String> kendraId, String applnType,
			String applnStatus);

	Optional<ApplicationMaster> findTopByAppIdAndApplicationIdAndKendraIdOrderByVersionNumDesc(String appId,
			String applicationId, String kendraId);

	List<ApplicationMaster> findByBranchCodeAndApplicationType(String branchCode, String applnType);

	Optional<ApplicationMaster> findTopByAppIdAndApplicationIdAndKendraIdAndVersionNum(String appId,
			String applicationId, String kendraId, String versionNo);

	List<ApplicationMaster> findByCreatedByAndApplicationType(String userId, String applicationType);

	Optional<ApplicationMaster> findTopByAppIdAndCreatedByAndApplicationIdAndApplicationTypeAndKendraIdAndBranchCodeOrderByVersionNumDesc(
			String appId, String userId, String applicationId, String applicationType, String kendraId,
			String branchId);

	List<ApplicationMaster> findByKendraIdAndApplicationDate(String kendraId, LocalDate applicationDate);

	List<ApplicationMaster> findByKendraIdInAndApplicationDateAndProductCodeAndApplicationStatus(List<String> kendraIds,
			LocalDate applicationDate, String productCode, String applicationStatus);

	Optional<ApplicationMaster> findByKendraIdAndApplicationDateAndAddInfoContaining(String kendraId,
			LocalDate applicationDate, String addInfo);

	List<ApplicationMaster> findByBranchCodeAndApplicationTypeOrderByVersionNumDesc(String branchCode,
			String applnType);

	List<ApplicationMaster> findByBranchCodeAndApplicationDateAndApplicationTypeOrderByVersionNumDesc(String branchCode,
			LocalDate applicationDate, String applnType);

	List<ApplicationMaster> findByBranchCodeAndApplicationTypeAndKmIdOrderByVersionNumDesc(String branchCode,
			String applnType, String kmId);

	List<ApplicationMaster> findByBranchCodeAndApplicationDateAndApplicationTypeAndKmIdOrderByVersionNumDesc(
			String branchCode, LocalDate applicationDate, String applnType, String kmId);
	
	Optional<ApplicationMaster> findTopByApplicationIdAndKendraIdAndApplicationDateOrderByVersionNumDesc(
			String applicationId, String kendraId, LocalDate applicationDate);

	Optional<ApplicationMaster> findByApplicationIdAndApplicationDateAndAddInfoContaining(String applicationId, LocalDate applicationDate, String addInfo);

	List<ApplicationMaster> findByKendraIdInAndApplicationDateAndProductCodeAndApplicationStatusIn(
			List<String> kendraIds, LocalDate applicationDate, String productCode, List<String> applicationStatuses);

	@Query(
			value = """
        SELECT * FROM tb_uaco_application_master am WHERE am.kendra_id IN (:kendraIds) AND am.application_date = :applicationDate
        AND am.application_type IS NOT NULL AND am.application_type <> 'INCOMEASSESSMENT'
        """, nativeQuery = true)
	List<ApplicationMaster> findApplicationsByKendraAndDate(
			@Param("kendraIds") List<String> kendraIds,
			@Param("applicationDate") LocalDate applicationDate
	);

	List<ApplicationMaster> findByApplicationIdAndApplicationDateAndVersionNum(String applicationId, LocalDate applicationDate, String versionNum);
	
	Optional<ApplicationMaster> findByApplicationIdAndVersionNum(String applicationId, String versionNum);

	@Modifying
	@Transactional
	@Query("""
       UPDATE ApplicationMaster a
       SET a.applicationStatus = :newStatus
       WHERE a.customerId = :memberId
       AND a.applicationStatus = 'INPROGRESS'
       """)
	int updateStatusIfInProgress(@Param("memberId") String memberId,
								 @Param("newStatus") String newStatus);


	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("""
        UPDATE ApplicationMaster am
        SET am.applicationStatus = :status
        WHERE am.customerId = :customerId
          AND am.applicationId = :applicationId
    """)
	int updateApplicationStatusByCustomerIdAndApplicationId(
			@Param("status") String status,
			@Param("customerId") String customerId,
			@Param("applicationId") String applicationId);

}
