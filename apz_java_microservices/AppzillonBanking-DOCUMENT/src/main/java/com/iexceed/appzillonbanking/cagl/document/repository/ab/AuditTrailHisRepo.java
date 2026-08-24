package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.document.domain.ab.AuditTrailEntityHis;

@Repository
public interface AuditTrailHisRepo extends JpaRepository<AuditTrailEntityHis, Integer> {

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.branch_id =:branchId", nativeQuery = true)
	List<AuditTrailEntityHis> findUserBasedOnBranchId(@Param("branchId") String branchId);

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.user_id =:userId", nativeQuery = true)
	List<AuditTrailEntityHis> findByUserIdBasedOnUserRole(@Param("userId") String userId);

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.kendraId IN (:kendraId)", nativeQuery = true)
	List<AuditTrailEntityHis> fetchAuditDetailsBasedOnKendraIds(@Param("kendraId") List<String> kendraId);

	/*
	 * @Query(value = "SELECT * FROM tb_uaco_audit_trail a " +
	 * "WHERE a.create_ts = (SELECT MAX(b.create_ts) FROM tb_uaco_audit_trail b WHERE b.application_id = a.application_id) "
	 * + "AND a.stage_id IN ('1', '2', '3', '4') " + "AND a.branch_id =:branchId",
	 * nativeQuery = true) List<AuditTrailEntity>
	 * findUserBasedOnBranchId(@Param("branchId") String branchId);
	 * 
	 * @Query(value = "SELECT * FROM tb_uaco_audit_trail a " +
	 * "WHERE a.create_ts = (SELECT MAX(b.create_ts) FROM tb_uaco_audit_trail b WHERE b.application_id = a.application_id) "
	 * + "AND a.stage_id IN ('1', '2', '3', '4') " + "AND a.user_id =:userId",
	 * nativeQuery = true) List<AuditTrailEntity>
	 * findByUserIdBasedOnUserRole(@Param("userId") String userId);
	 */

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.application_id =:applicationId", nativeQuery = true)
	List<AuditTrailEntityHis> findAllTrailDetails(@Param("applicationId") String applicationId);

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.application_id =:applicationId  ORDER BY create_ts DESC LIMIT 1", nativeQuery = true)
	AuditTrailEntityHis findApplicationId(@Param("applicationId") String applicationId);

	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.application_id =:applicationId  ORDER BY create_ts DESC LIMIT 1", nativeQuery = true)
	Optional<AuditTrailEntityHis> findTopByApplicationId(String applicationId);

	// Surbhi ~ changes 24 March
	@Query(value = "SELECT * FROM tb_uaco_audit_trail_history a WHERE a.application_id = :applicationId AND a.stage_id = :stageId ORDER BY a.create_ts ASC LIMIT 1", nativeQuery = true)
	AuditTrailEntityHis findLatestByApplicationIdAndStageId(@Param("applicationId") String applicationId,
			@Param("stageId") String stageId);

}
