package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerApplicationMaster;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.CustomerApplicationMasterId;
import com.iexceed.appzillonbanking.cagl.loan.payload.ApplicationNomineeProjection;

@Repository
public interface CustomerApplicationMasterRepository
		extends JpaRepository<CustomerApplicationMaster, CustomerApplicationMasterId> {

	@Query(value = """
			    SELECT
			        -- Application
			        app.branch_name as branchName,
			        app.request_type as requestType,
			        app.create_ts as createTs,
			        app.updated_ts as updatedTs,
			        app.latest_version_no as latestVersionNo,
			        app.kendra_id as kendraId,
			        app.customer_id as customerId,
			        app.created_by as createdBy,
			        app.updated_by as updatedBy,
			        app.application_type as applicationType,
			        app.kyc_type as kycType,
			        app.application_status as applicationStatus,
			        app.branch_code as branchCode,
			        app.current_screen_id as currentScreenId,
			        app.remarks as remarks,
			        app.current_stage as currentStage,
			        app.kmid as kmid,
			        app.kendraname as kendraname,
			        app.customer_name as customerName,
			        app.add_info1 as addInfo1,
			        app.app_id as appId,
			        app.loan_application_no as loanApplicationNo,
			        app.application_id as applicationId,

			        -- Nominee
			        nom.create_ts as nomineeCreateTs,
			        nom.updated_ts as nomineeUpdatedTs,
			        nom.customer_id as nomineeCustomerId,
			        nom.relationtype as relationtype,
			        nom.memrelation as memrelation,
			        nom.legaldocname as legaldocname,
			        nom.legaldocid as legaldocid,
			        nom.inputdata as inputdata,
			        nom.docunof as docunof,
			        nom.docunob as docunob,
			        nom.reason as reason,
			        nom.ocrresponsepayload as ocrresponsepayload,
			        nom.nomineedtls as nomineedtls,
			        nom.ocrdetails as ocrdetails,
			        nom.customer_name as nomineeCustomerName,
			        nom.application_id as nomineeApplicationId,
			        nom.add_info as nomineeAddInfo,
			        nom.latest_version_no as nomineeLatestVersionNo
			    FROM tb_ucao_customer_application_master app
			    LEFT JOIN tb_ucno_customer_nominee_details nom
			        ON app.application_id = nom.application_id
			        AND app.customer_id = nom.customer_id
			    WHERE (:applicationId IS NULL OR app.application_id = :applicationId)
			    AND (:customerId IS NULL OR app.customer_id = :customerId)
			""", nativeQuery = true)
	List<ApplicationNomineeProjection> fetchFullData(@Param("applicationId") String applicationId,
			@Param("customerId") String customerId);

	@Query("SELECT app, nom FROM CustomerApplicationMaster app " + "LEFT JOIN CustomerNomineeDetails nom "
			+ "ON app.application_id = nom.application_id " + "AND app.customer_id = nom.customer_id "
			+ "AND app.latest_version_no = nom.latest_version_no " + "WHERE app.branch_code IN :branchId")
	List<Object[]> fetchNomineeWithDetailsBasedOnBranchIds(@Param("branchId") List<String> branchId);

	@Query("SELECT app, nom FROM CustomerApplicationMaster app " + "LEFT JOIN CustomerNomineeDetails nom "
			+ "ON app.application_id = nom.application_id " + "AND app.customer_id = nom.customer_id "
			+ "WHERE app.kendra_id IN :kendraId")
	List<Object[]> fetchNomineeWithDetailsbasedOnKendraIds(@Param("kendraId") List<String> kendraId);

	@Query("SELECT app, nom FROM CustomerApplicationMaster app " + "LEFT JOIN CustomerNomineeDetails nom "
			+ "ON app.application_id = nom.application_id " + "AND app.customer_id = nom.customer_id "
			+ "AND app.latest_version_no = nom.latest_version_no " + "WHERE app.application_id IN :applicationId")
	List<Object[]> fetchNomineeWithDetailsbasedOnApplicationId(@Param("applicationId") String applicationId);

	@Query("SELECT app, nom FROM CustomerApplicationMaster app " + "LEFT JOIN CustomerNomineeDetails nom "
			+ "ON app.application_id = nom.application_id " + "AND app.customer_id = nom.customer_id "
			+ "AND app.latest_version_no = nom.latest_version_no "
			+ "WHERE app.application_status IN ('ON HOLD', 'PENDING', 'MOVE TO CHECKER') "
			+ "AND app.branch_code IN :branchId")
	List<Object[]> fetchNomineeWithStatusAndBranchIds(@Param("branchId") List<String> branchId);
}
