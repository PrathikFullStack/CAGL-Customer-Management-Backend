package com.iexceed.appzillonbanking.cagl.cm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cm.entity.cdh.GkUnifiedDataEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmAddressEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustAuditTrailEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmDocumentEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmFamilyMemberEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmRecordLockEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.ActiveLoansCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.AddressCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.AddressCardDto.AddressItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.BankDetailsCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto.LockStatusDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.FamilyDetailsCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.FamilyDetailsCardDto.FamilyMemberItemDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.FamilyDetailsCardDto.NomineeBankDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.FamilyDetailsCardDto.NomineeDetailDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.FamilyDetailsCardDto.SpouseDetailDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.IncomeAssessmentCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.KycDetailsCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.ProfileHeaderCardDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.RecentAuditChangeDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.cdh.GkUnifiedDataRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmAddressRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustAuditTrailRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmFamilyMemberRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmRecordLockRepository;

@Service
public class CustomerProfileService {

    private static final Logger logger = LogManager.getLogger(CustomerProfileService.class);

    private final CmCustomerRepository customerRepo;
    private final CmAddressRepository addressRepo;
    private final CmFamilyMemberRepository familyRepo;
    private final CmApplicationMasterRepository appRepo;
    private final CmDocumentRepository docRepo;
    private final CmRecordLockRepository lockRepo;
    private final CmCustAuditTrailRepository auditRepo;
    private final GkUnifiedDataRepository cdhRepo;
    private final CdhIngestionService ingestionService;
    private final ObjectMapper objectMapper;

    public CustomerProfileService(
            CmCustomerRepository customerRepo,
            CmAddressRepository addressRepo,
            CmFamilyMemberRepository familyRepo,
            CmApplicationMasterRepository appRepo,
            CmDocumentRepository docRepo,
            CmRecordLockRepository lockRepo,
            CmCustAuditTrailRepository auditRepo,
            GkUnifiedDataRepository cdhRepo,
            CdhIngestionService ingestionService,
            ObjectMapper objectMapper) {
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.familyRepo = familyRepo;
        this.appRepo = appRepo;
        this.docRepo = docRepo;
        this.lockRepo = lockRepo;
        this.auditRepo = auditRepo;
        this.cdhRepo = cdhRepo;
        this.ingestionService = ingestionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Loads full consolidated profile response DTO for UI screens
     */
    public Optional<CustomerProfileResponseDto> getCustomerProfile(String customerId, String userId) {
        logger.debug("Fetching customer profile for Customer ID: {}", customerId);

        Optional<CmCustomerEntity> custOpt = customerRepo.findByCustomerId(customerId);
        if (custOpt.isEmpty()) {
            // Auto-ingest from CDH if not yet in local tb_cm_*
            custOpt = ingestionService.ingestFromCdh(customerId, userId);
        }

        if (custOpt.isEmpty()) {
            logger.warn("Customer: {} not found in local CM or CDH", customerId);
            return Optional.empty();
        }

        CmCustomerEntity cust = custOpt.get();
        Optional<CmApplicationMasterEntity> appOpt = appRepo.findByCustomerId(customerId);
        List<CmAddressEntity> addressList = addressRepo.findByCustomerId(customerId);
        List<CmFamilyMemberEntity> familyList = familyRepo.findByCustomerId(customerId);
        List<CmCustAuditTrailEntity> last3Audits = auditRepo.findLast3ChangesByCustomerId(customerId);
        Optional<GkUnifiedDataEntity> cdhOpt = cdhRepo.findByCustomerId(customerId);

        // 1. Build Header Card
        ProfileHeaderCardDto header = ProfileHeaderCardDto.builder()
                .customerId(cust.getCustomerId())
                .customerName(cust.getCustomerName())
                .mobileNumber(appOpt.map(CmApplicationMasterEntity::getMobileNumber).orElse(null))
                .dob(cust.getDob())
                .maritalStatus(cust.getMaritalStatus())
                .profileCompletionPercentage(85)
                .customerStatus(cust.getKycStatus())
                .kycRenewalRequired(false)
                .pendingUpdatesCount(0)
                .inProgressUpdatesCount(0)
                .build();

        // 2. Build KYC Details Card
        KycDetailsCardDto kycDetails = KycDetailsCardDto.builder()
                .primaryKycType(cust.getPrimaryKycType())
                .primaryKycId(cust.getPrimaryKycId())
                .aadhaarMasked(cdhOpt.map(GkUnifiedDataEntity::getAadhaarNumberMasked).orElse(null))
                .panNumber(cdhOpt.map(GkUnifiedDataEntity::getPanNumber).orElse(null))
                .ckycId(cdhOpt.map(GkUnifiedDataEntity::getCkycId).orElse(null))
                .kycStatus(cust.getKycStatus())
                .kycValidationStatus("VERIFIED")
                .build();

        // 3. Build Addresses Card
        AddressItemDto perm = null;
        AddressItemDto comm = null;
        for (CmAddressEntity addr : addressList) {
            try {
                AddressItemDto item = objectMapper.readValue(addr.getAddrPayload(), AddressItemDto.class);
                if ("P".equalsIgnoreCase(addr.getAddressType())) perm = item;
                if ("C".equalsIgnoreCase(addr.getAddressType())) comm = item;
            } catch (Exception ignored) {}
        }
        AddressCardDto addresses = AddressCardDto.builder()
                .permanentAddress(perm)
                .communicationAddress(comm)
                .commSameAsPerm(false)
                .houseLatitude(cdhOpt.map(c -> c.getHouseLatitude() != null ? c.getHouseLatitude().toString() : null).orElse(null))
                .houseLongitude(cdhOpt.map(c -> c.getHouseLongitude() != null ? c.getHouseLongitude().toString() : null).orElse(null))
                .build();

        // 4. Build Bank Details Card
        BankDetailsCardDto bankDetails = null;
        if (cust.getBankDetails() != null) {
            try {
                Map<String, Object> bMap = objectMapper.readValue(cust.getBankDetails(), new TypeReference<>() {});
                bankDetails = BankDetailsCardDto.builder()
                        .bankName((String) bMap.get("bankName"))
                        .bankBranchName((String) bMap.get("bankBranchName"))
                        .ifscCode((String) bMap.get("ifscCode"))
                        .bankAccountNumberMasked((String) bMap.get("accountNumber"))
                        .accountHolderName((String) bMap.get("accountHolderName"))
                        .bankVerificationStatus((String) bMap.get("verificationStatus"))
                        .bankVerifiedDate((String) bMap.get("verifiedDate"))
                        .build();
            } catch (Exception ignored) {}
        }

        // 5. Build Family & Nominee Card
        SpouseDetailDto spouse = null;
        NomineeDetailDto nominee = null;
        List<FamilyMemberItemDto> famItems = new ArrayList<>();
        for (CmFamilyMemberEntity fam : familyList) {
            if ("SP".equalsIgnoreCase(fam.getMemberType())) {
                spouse = SpouseDetailDto.builder()
                        .name(fam.getName())
                        .gender(fam.getGender())
                        .kycType(fam.getKycType())
                        .kycDocId(fam.getKycDocId())
                        .build();
            } else if (Boolean.TRUE.equals(fam.getIsNominee())) {
                nominee = NomineeDetailDto.builder()
                        .name(fam.getName())
                        .relation(fam.getRelation())
                        .docType(fam.getKycType())
                        .docId(fam.getKycDocId())
                        .build();
            }
            famItems.add(FamilyMemberItemDto.builder()
                    .familyMemId(fam.getFamilyMemId())
                    .memberType(fam.getMemberType())
                    .relation(fam.getRelation())
                    .name(fam.getName())
                    .gender(fam.getGender())
                    .kycType(fam.getKycType())
                    .kycDocId(fam.getKycDocId())
                    .isNominee(Boolean.TRUE.equals(fam.getIsNominee()))
                    .isEarningMember(Boolean.TRUE.equals(fam.getIsEarningMember()))
                    .build());
        }
        FamilyDetailsCardDto familyDetails = FamilyDetailsCardDto.builder()
                .spouse(spouse)
                .nominee(nominee)
                .familyMembers(famItems)
                .build();

        // 6. Build Income Assessment Card
        IncomeAssessmentCardDto income = cdhOpt.map(c -> IncomeAssessmentCardDto.builder()
                .totalIncome(c.getTotalIncome())
                .totalExpenses(c.getTotalExpenses())
                .sourceOfIncome(c.getSourceOfIncome())
                .wetLandAcres(c.getWetLandAcres() != null ? c.getWetLandAcres().toString() : null)
                .dryLandAcres(c.getDryLandAcres() != null ? c.getDryLandAcres().toString() : null)
                .religion(c.getReligion())
                .caste(c.getCaste())
                .nationality(c.getNationality())
                .noOfAdults(c.getNoOfAdults())
                .noOfChildren(c.getNoOfChildren())
                .build()).orElse(null);

        // 7. Recent Audits
        List<RecentAuditChangeDto> audits = new ArrayList<>();
        for (CmCustAuditTrailEntity a : last3Audits) {
            audits.add(RecentAuditChangeDto.builder()
                    .changeId(a.getId())
                    .updatedBy(a.getUserName())
                    .userRole(a.getUserRole())
                    .timestamp(a.getCreateTs() != null ? a.getCreateTs().toString() : null)
                    .stage(a.getStageId())
                    .editedFieldsJson(a.getEditeddetails())
                    .build());
        }

        // 8. Lock Status
        String appId = cust.getApplicationId();
        Optional<CmRecordLockEntity> lockOpt = lockRepo.findActiveLock(appId, LocalDateTime.now());
        LockStatusDto lockStatus = LockStatusDto.builder()
                .isLocked(lockOpt.isPresent())
                .lockedBy(lockOpt.map(CmRecordLockEntity::getLockedBy).orElse(null))
                .lockedByRole(lockOpt.map(CmRecordLockEntity::getLockedByRole).orElse(null))
                .lockExpiry(lockOpt.map(l -> l.getLockExpiryTs().toString()).orElse(null))
                .build();

        return Optional.of(CustomerProfileResponseDto.builder()
                .header(header)
                .kycDetails(kycDetails)
                .addresses(addresses)
                .bankDetails(bankDetails)
                .familyDetails(familyDetails)
                .incomeAssessment(income)
                .recentChanges(audits)
                .lockStatus(lockStatus)
                .build());
    }
}
