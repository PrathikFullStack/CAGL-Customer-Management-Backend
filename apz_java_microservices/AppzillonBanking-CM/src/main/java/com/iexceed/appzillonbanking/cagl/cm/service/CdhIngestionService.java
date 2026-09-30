package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.cdh.GkUnifiedDataEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmAddressEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmFamilyMemberEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.cdh.GkUnifiedDataRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmAddressRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmFamilyMemberRepository;

@Service
public class CdhIngestionService {

    private static final Logger logger = LogManager.getLogger(CdhIngestionService.class);

    private final GkUnifiedDataRepository cdhRepo;
    private final CmCustomerRepository customerRepo;
    private final CmAddressRepository addressRepo;
    private final CmFamilyMemberRepository familyRepo;
    private final CmApplicationMasterRepository appRepo;
    private final ObjectMapper objectMapper;

    public CdhIngestionService(
            GkUnifiedDataRepository cdhRepo,
            CmCustomerRepository customerRepo,
            CmAddressRepository addressRepo,
            CmFamilyMemberRepository familyRepo,
            CmApplicationMasterRepository appRepo,
            ObjectMapper objectMapper) {
        this.cdhRepo = cdhRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.familyRepo = familyRepo;
        this.appRepo = appRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Ingests a customer from CDH (gk_unified_data) into normalized tb_cm_* tables
     */
    @Transactional("primaryTransactionManager")
    public Optional<CmCustomerEntity> ingestFromCdh(String customerId, String userId) {
        logger.info("Starting CDH ingestion for Customer ID: {}", customerId);

        Optional<GkUnifiedDataEntity> cdhOpt = cdhRepo.findByCustomerId(customerId);
        if (cdhOpt.isEmpty()) {
            logger.warn("Customer ID: {} not found in CDH gk_unified_data", customerId);
            return Optional.empty();
        }

        GkUnifiedDataEntity cdh = cdhOpt.get();
        String applicationId = "APP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. Bank Details JSON
            Map<String, Object> bankMap = new HashMap<>();
            bankMap.put("bankName", cdh.getBankName());
            bankMap.put("bankBranchName", cdh.getBankBranchName());
            bankMap.put("ifscCode", cdh.getBankIfscCode());
            bankMap.put("accountNumber", cdh.getBankAccountNumber());
            bankMap.put("accountHolderName", cdh.getBankAccountName());
            bankMap.put("verificationStatus", cdh.getBankVerificationStatus());
            bankMap.put("verifiedDate", cdh.getBankVerifiedDate());
            String bankJson = objectMapper.writeValueAsString(bankMap);

            // 2. KYC Details JSON
            Map<String, Object> kycMap = new HashMap<>();
            kycMap.put("primaryType", cdh.getPrimaryType());
            kycMap.put("primaryId", cdh.getPrimaryId());
            kycMap.put("aadhaarMasked", cdh.getAadhaarNumberMasked());
            kycMap.put("panNumber", cdh.getPanNumber());
            kycMap.put("ckycId", cdh.getCkycId());
            String kycJson = objectMapper.writeValueAsString(kycMap);

            // 3. Save CmCustomerEntity (tb_cm_customer)
            CmCustomerEntity customer = CmCustomerEntity.builder()
                    .customerId(cdh.getCustomerId())
                    .applicationId(applicationId)
                    .customerName(cdh.getCustomerName())
                    .dob(cdh.getDob())
                    .maritalStatus(cdh.getMaritalStatus())
                    .livePhotoStatus("PENDING")
                    .kycStatus("APPROVED")
                    .primaryKycType(cdh.getPrimaryType() != null ? cdh.getPrimaryType() : "AADHAAR")
                    .primaryKycId(cdh.getPrimaryId())
                    .amlStatus("PASS")
                    .breStatus("PASS")
                    .kycDetails(kycJson)
                    .bankDetails(bankJson)
                    .createdBy(userId != null ? userId : "SYSTEM_CDH")
                    .createdTs(now)
                    .build();
            customer = customerRepo.save(customer);

            // 4. Save Permanent Address (tb_cm_address - 'P')
            Map<String, Object> permAddrMap = new HashMap<>();
            permAddrMap.put("line1", cdh.getPermanentAddressLine1());
            permAddrMap.put("line2", cdh.getPermanentAddressLine2());
            permAddrMap.put("line3", cdh.getPermanentAddressLine3());
            permAddrMap.put("villageLocality", cdh.getPermanentVillageLocality());
            permAddrMap.put("taluk", cdh.getPermanentTaluk());
            permAddrMap.put("district", cdh.getPermanentDistrict());
            permAddrMap.put("state", cdh.getPermanentState());
            permAddrMap.put("pincode", cdh.getPermanentPincode());

            CmAddressEntity permAddr = CmAddressEntity.builder()
                    .addressId("ADDR_P_" + UUID.randomUUID().toString().substring(0, 6))
                    .customerId(cdh.getCustomerId())
                    .applicationId(applicationId)
                    .addressType("P")
                    .commSameAsPerm("N")
                    .addrPayload(objectMapper.writeValueAsString(permAddrMap))
                    .distanceFromBranch(cdh.getDistanceFromBranch() != null ? cdh.getDistanceFromBranch().toString() : null)
                    .createdTs(now)
                    .build();
            addressRepo.save(permAddr);

            // 5. Save Communication Address (tb_cm_address - 'C')
            Map<String, Object> commAddrMap = new HashMap<>();
            commAddrMap.put("line1", cdh.getCommunicationAddressLine1());
            commAddrMap.put("line2", cdh.getCommunicationAddressLine2());
            commAddrMap.put("line3", cdh.getCommunicationAddressLine3());
            commAddrMap.put("villageLocality", cdh.getCommunicationVillageLocality());
            commAddrMap.put("taluk", cdh.getCommunicationTaluk());
            commAddrMap.put("district", cdh.getCommunicationDistrict());
            commAddrMap.put("state", cdh.getCommunicationState());
            commAddrMap.put("pincode", cdh.getCommunicationPincode());

            CmAddressEntity commAddr = CmAddressEntity.builder()
                    .addressId("ADDR_C_" + UUID.randomUUID().toString().substring(0, 6))
                    .customerId(cdh.getCustomerId())
                    .applicationId(applicationId)
                    .addressType("C")
                    .commSameAsPerm("N")
                    .addrPayload(objectMapper.writeValueAsString(commAddrMap))
                    .createdTs(now)
                    .build();
            addressRepo.save(commAddr);

            // 6. Save Dependent/Family & Nominee (tb_cm_family_member)
            if (cdh.getDepName() != null && !cdh.getDepName().isBlank()) {
                CmFamilyMemberEntity dep = CmFamilyMemberEntity.builder()
                        .familyMemId("FAM_" + UUID.randomUUID().toString().substring(0, 6))
                        .customerId(cdh.getCustomerId())
                        .applicationId(applicationId)
                        .memberType("SP")
                        .relation(cdh.getMemRelation() != null ? cdh.getMemRelation() : "Spouse")
                        .name(cdh.getDepName())
                        .kycType(cdh.getDepDocType())
                        .kycDocId(cdh.getDepDocId())
                        .isNominee(false)
                        .isEarningMember(false)
                        .createdTs(now)
                        .build();
                familyRepo.save(dep);
            }

            if (cdh.getNomineeName() != null && !cdh.getNomineeName().isBlank()) {
                CmFamilyMemberEntity nom = CmFamilyMemberEntity.builder()
                        .familyMemId("FAM_" + UUID.randomUUID().toString().substring(0, 6))
                        .customerId(cdh.getCustomerId())
                        .applicationId(applicationId)
                        .memberType("CO")
                        .relation(cdh.getNomineeRelation() != null ? cdh.getNomineeRelation() : "Nominee")
                        .name(cdh.getNomineeName())
                        .kycType(cdh.getNomineeDocType())
                        .kycDocId(cdh.getNomineeDocId())
                        .isNominee(true)
                        .isEarningMember(false)
                        .createdTs(now)
                        .build();
                familyRepo.save(nom);
            }

            // 7. Save Application Master (tb_cm_application_master)
            CmApplicationMasterEntity app = CmApplicationMasterEntity.builder()
                    .applicationId(applicationId)
                    .customerId(cdh.getCustomerId())
                    .version("1")
                    .customerName(cdh.getCustomerName())
                    .mobileNumber(cdh.getMobileNumber())
                    .branchId(cdh.getBranchId() != null ? cdh.getBranchId() : "BR01")
                    .branchName(cdh.getBranchName() != null ? cdh.getBranchName() : "Branch")
                    .kendraId(cdh.getKendraId() != null ? cdh.getKendraId().toString() : null)
                    .kendraName(cdh.getKendraName())
                    .groupId(cdh.getGroupId() != null ? cdh.getGroupId().toString() : null)
                    .kmName(cdh.getKmName() != null ? cdh.getKmName() : "KM Officer")
                    .stage("DATA_INGESTED")
                    .subStage("CDH_SYNCED")
                    .status("ACTIVE")
                    .recordType("EXISTING_MEMBER")
                    .loanEligible(cdh.getEligibleCaglAmount() != null ? "Y" : "N")
                    .loanId(cdh.getLoanId())
                    .createdBy(userId != null ? userId : "SYSTEM_CDH")
                    .createdTs(now)
                    .build();
            appRepo.save(app);

            logger.info("Successfully ingested customer: {} into tb_cm_* tables with App ID: {}", customerId, applicationId);
            return Optional.of(customer);

        } catch (Exception ex) {
            logger.error("Error ingesting CDH record for customer {}: {}", customerId, ex.getMessage(), ex);
            throw new RuntimeException("CDH Ingestion failed", ex);
        }
    }
}
