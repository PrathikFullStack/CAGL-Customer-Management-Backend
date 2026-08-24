package com.iexceed.appzillonbanking.cagl.loan.repository.ab;

import com.iexceed.appzillonbanking.cagl.loan.payload.NomineeInsuranceApplicationDto;
import com.iexceed.appzillonbanking.cagl.loan.domain.ab.ApplicationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationMasterCustomRepo extends JpaRepository<ApplicationMaster, String> {

    @Query(value = """
        SELECT 
            app_id as appId,
            application_id as applicationId,
            latest_version_no as versionNum,
            kendra_id as kendraId,
            customer_id as customerId,
            application_date as applicationDate,
            create_ts as createTs,
            created_by as createdBy,
            application_type as applicationType,
            kyc_type as kycType,
            application_status as applicationStatus,
            product_group_code as productGroupCode,
            product_code as productCode,
            branch_code as branchCode,
            current_screen_id as currentScreenId,
            remarks as remarks,
            cb_check as cbCheck,
            current_stage as currentStage,
            kmid as kmId,
            leader as leader,
            kendraname as kendraName,
            loanmode as loanMode,
            amount as amount,
            customer_name as customerName,
            add_info as addInfo,
            application_ref_no as applicationRefNo,
            cb_approve_manual as cbApproveManual
        FROM tb_uaco_application_master
        WHERE customer_id = :memberId
        AND LOWER(application_status) = 'disbursed'
    """, nativeQuery = true)
    List<NomineeInsuranceApplicationDto> findByMemberId(String memberId);


    @Query(value = """
        SELECT 
            app_id as appId,
            application_id as applicationId,
            latest_version_no as versionNum,
            kendra_id as kendraId,
            customer_id as customerId,
            application_date as applicationDate,
            create_ts as createTs,
            created_by as createdBy,
            application_type as applicationType,
            kyc_type as kycType,
            application_status as applicationStatus,
            product_group_code as productGroupCode,
            product_code as productCode,
            branch_code as branchCode,
            current_screen_id as currentScreenId,
            remarks as remarks,
            cb_check as cbCheck,
            current_stage as currentStage,
            kmid as kmId,
            leader as leader,
            kendraname as kendraName,
            loanmode as loanMode,
            amount as amount,
            customer_name as customerName,
            add_info as addInfo,
            application_ref_no as applicationRefNo,
            cb_approve_manual as cbApproveManual
        FROM tb_uaco_application_master
        WHERE application_ref_no = :loanId
    """, nativeQuery = true)
    List<NomineeInsuranceApplicationDto> findByLoanId(String loanId);


    @Query(value = """
        SELECT 
            app_id as appId,
            application_id as applicationId,
            latest_version_no as versionNum,
            kendra_id as kendraId,
            customer_id as customerId,
            application_date as applicationDate,
            create_ts as createTs,
            created_by as createdBy,
            application_type as applicationType,
            kyc_type as kycType,
            application_status as applicationStatus,
            product_group_code as productGroupCode,
            product_code as productCode,
            branch_code as branchCode,
            current_screen_id as currentScreenId,
            remarks as remarks,
            cb_check as cbCheck,
            current_stage as currentStage,
            kmid as kmId,
            leader as leader,
            kendraname as kendraName,
            loanmode as loanMode,
            amount as amount,
            customer_name as customerName,
            add_info as addInfo,
            application_ref_no as applicationRefNo,
            cb_approve_manual as cbApproveManual
        FROM tb_uaco_application_master
        WHERE customer_id = :memberId
        AND application_ref_no = :loanId
        AND LOWER(application_status) = 'disbursed'
    """, nativeQuery = true)
    List<NomineeInsuranceApplicationDto> findByMemberAndLoan(String memberId, String loanId);
}