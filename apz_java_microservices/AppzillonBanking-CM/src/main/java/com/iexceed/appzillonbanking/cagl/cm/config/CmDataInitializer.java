package com.iexceed.appzillonbanking.cagl.cm.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmAddressEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmFamilyMemberEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmAddressRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmFamilyMemberRepository;

@Component
public class CmDataInitializer implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(CmDataInitializer.class);

    private final CmApplicationMasterRepository appRepo;
    private final CmCustomerRepository customerRepo;
    private final CmAddressRepository addressRepo;
    private final CmFamilyMemberRepository familyRepo;

    public CmDataInitializer(
            CmApplicationMasterRepository appRepo,
            CmCustomerRepository customerRepo,
            CmAddressRepository addressRepo,
            CmFamilyMemberRepository familyRepo) {
        this.appRepo = appRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.familyRepo = familyRepo;
    }

    @Override
    @Transactional("primaryTransactionManager")
    public void run(String... args) {
        if (appRepo.existsById("APP100294")) {
            logger.info("Seed data already present in tb_cm_application_master. Skipping auto-initialization.");
            return;
        }

        logger.info("Initializing baseline seed data in Customer Management database...");

        // 1. Seed Application Master Records
        List<CmApplicationMasterEntity> apps = List.of(
                // Drafts (2)
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100294")
                        .customerId("100294")
                        .version("1")
                        .customerName("Sunita Gowda")
                        .mobileNumber("9876543201")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .leader("Y")
                        .stage("DRAFT")
                        .wfstage("DRAFT")
                        .status("PENDING")
                        .subStage("DOC_UPLOAD")
                        .recordType("KYC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .loanId("LN900001")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .updatedBy("8282828230")
                        .updatedTs(LocalDateTime.now().minusHours(2))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100301")
                        .customerId("100301")
                        .version("1")
                        .customerName("Lakshmi Devi")
                        .mobileNumber("9876543202")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN102")
                        .kendraName("Bangalore")
                        .groupId("GRP02")
                        .kmName("8282828237")
                        .leader("N")
                        .stage("DRAFT")
                        .wfstage("DRAFT")
                        .status("PENDING")
                        .subStage("DOC_UPLOAD")
                        .recordType("BANK_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .loanId("LN900002")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .updatedBy("8282828237")
                        .updatedTs(LocalDateTime.now().minusHours(1))
                        .build(),

                // Onhold (4)
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100302")
                        .customerId("100302")
                        .version("1")
                        .customerName("Savitha Rao")
                        .mobileNumber("9876543203")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .stage("BMONHOLD")
                        .wfstage("BMONHOLD")
                        .status("ON_HOLD")
                        .recordType("LOC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(4))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100303")
                        .customerId("100303")
                        .version("1")
                        .customerName("Geetha Kumari")
                        .mobileNumber("9876543204")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .stage("BMONHOLD")
                        .wfstage("BMONHOLD")
                        .status("ON_HOLD")
                        .recordType("KYC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(4))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100304")
                        .customerId("100304")
                        .version("1")
                        .customerName("Roopa Shetty")
                        .mobileNumber("9876543205")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN102")
                        .kendraName("Bangalore")
                        .groupId("GRP02")
                        .kmName("8282828237")
                        .stage("RPCONHOLD")
                        .wfstage("RPCONHOLD")
                        .status("ON_HOLD")
                        .recordType("BANK_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(3))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100305")
                        .customerId("100305")
                        .version("1")
                        .customerName("Manjula N")
                        .mobileNumber("9876543206")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN102")
                        .kendraName("Bangalore")
                        .groupId("GRP02")
                        .kmName("8282828237")
                        .stage("AMONHOLD")
                        .wfstage("AMONHOLD")
                        .status("ON_HOLD")
                        .recordType("FAM_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(3))
                        .build(),

                // Pending for BM review (2)
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100306")
                        .customerId("100306")
                        .version("1")
                        .customerName("Kavitha M")
                        .mobileNumber("9876543207")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .stage("BMQUEUE")
                        .wfstage("BMQUEUE")
                        .status("IN_REVIEW")
                        .recordType("KYC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(2))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100307")
                        .customerId("100307")
                        .version("1")
                        .customerName("Pushpa L")
                        .mobileNumber("9876543208")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN102")
                        .kendraName("Bangalore")
                        .groupId("GRP02")
                        .kmName("8282828237")
                        .stage("BMQUEUE")
                        .wfstage("BMQUEUE")
                        .status("IN_REVIEW")
                        .recordType("BANK_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(2))
                        .build(),

                // Pending for RPC review (2)
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100310")
                        .customerId("100310")
                        .version("1")
                        .customerName("Suma B")
                        .mobileNumber("9876543211")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .stage("RPCMAKERQUEUE")
                        .wfstage("RPCMAKERQUEUE")
                        .status("IN_REVIEW")
                        .recordType("KYC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(1))
                        .build(),
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100311")
                        .customerId("100311")
                        .version("1")
                        .customerName("Radha K")
                        .mobileNumber("9876543212")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN102")
                        .kendraName("Bangalore")
                        .groupId("GRP02")
                        .kmName("8282828237")
                        .stage("RPCCHECKERQUEUE")
                        .wfstage("RPCCHECKERQUEUE")
                        .status("IN_REVIEW")
                        .recordType("BANK_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("ELIGIBLE")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(1))
                        .build(),

                // Rejected (1)
                CmApplicationMasterEntity.builder()
                        .applicationId("APP100314")
                        .customerId("100314")
                        .version("1")
                        .customerName("Deepa G")
                        .mobileNumber("9876543215")
                        .branchId("Ejipura")
                        .branchName("Ejipura Branch")
                        .kendraId("KEN101")
                        .kendraName("Mandya")
                        .groupId("GRP01")
                        .kmName("8282828230")
                        .stage("REJECTED")
                        .wfstage("REJECTED")
                        .status("REJECTED")
                        .recordType("KYC_UPDATE")
                        .channelType("ONLINE")
                        .loanEligible("NOT_ELIGIBLE")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(1))
                        .build()
        );
        appRepo.saveAll(apps);

        // 2. Seed Customer Core Profiles
        List<CmCustomerEntity> customers = List.of(
                CmCustomerEntity.builder()
                        .customerId("100294")
                        .applicationId("APP100294")
                        .customerName("Sunita Gowda")
                        .dob("1988-06-15")
                        .maritalStatus("Married")
                        .primaryKycType("AADHAAR")
                        .primaryKycId("XXXXXXXX1001")
                        .kycStatus("VERIFIED")
                        .livePhotoStatus("CAPTURED")
                        .amlStatus("CLEAR")
                        .breStatus("PASS")
                        .cgtStatus("COMPLETED")
                        .grtStatus("COMPLETED")
                        .bankDetails("{\"bankName\":\"State Bank of India\",\"accountNumber\":\"30123456781\",\"ifscCode\":\"SBIN0001234\",\"bankBranchName\":\"Mandya\",\"verificationStatus\":\"VERIFIED\"}")
                        .createdBy("8282828230")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build(),
                CmCustomerEntity.builder()
                        .customerId("100301")
                        .applicationId("APP100301")
                        .customerName("Lakshmi Devi")
                        .dob("1985-04-12")
                        .maritalStatus("Married")
                        .primaryKycType("AADHAAR")
                        .primaryKycId("XXXXXXXX1002")
                        .kycStatus("VERIFIED")
                        .livePhotoStatus("CAPTURED")
                        .amlStatus("CLEAR")
                        .breStatus("PASS")
                        .cgtStatus("COMPLETED")
                        .grtStatus("COMPLETED")
                        .bankDetails("{\"bankName\":\"Canara Bank\",\"accountNumber\":\"0412101002\",\"ifscCode\":\"CNRB0000412\",\"bankBranchName\":\"Bangalore\",\"verificationStatus\":\"VERIFIED\"}")
                        .createdBy("8282828237")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build()
        );
        customerRepo.saveAll(customers);

        // 3. Seed Addresses
        List<CmAddressEntity> addresses = List.of(
                CmAddressEntity.builder()
                        .addressId("ADR100294")
                        .customerId("100294")
                        .applicationId("APP100294")
                        .addressType("P")
                        .addrPayload("{\"line1\":\"No 12, Main Road\",\"line2\":\"Mandya Town\",\"line3\":\"\",\"village\":\"Mandya\",\"taluk\":\"Mandya\",\"district\":\"Mandya\",\"state\":\"Karnataka\",\"pincode\":\"571401\"}")
                        .addressProofDocId("DOC1001")
                        .addressProofType("AADHAAR")
                        .commSameAsPerm("Y")
                        .distanceFromBranch("1.5")
                        .subType("OWNED")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build(),
                CmAddressEntity.builder()
                        .addressId("ADR100301")
                        .customerId("100301")
                        .applicationId("APP100301")
                        .addressType("P")
                        .addrPayload("{\"line1\":\"No 45, 5th Main\",\"line2\":\"Ejipura\",\"line3\":\"\",\"village\":\"Ejipura\",\"taluk\":\"Bengaluru South\",\"district\":\"Bengaluru Urban\",\"state\":\"Karnataka\",\"pincode\":\"560047\"}")
                        .addressProofDocId("DOC1002")
                        .addressProofType("AADHAAR")
                        .commSameAsPerm("Y")
                        .distanceFromBranch("2.0")
                        .subType("RENTED")
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build()
        );
        addressRepo.saveAll(addresses);

        // 4. Seed Family Members
        List<CmFamilyMemberEntity> familyMembers = List.of(
                CmFamilyMemberEntity.builder()
                        .familyMemId("FAM100294")
                        .customerId("100294")
                        .applicationId("APP100294")
                        .memberType("SP")
                        .name("Ramesh Gowda")
                        .relation("Spouse")
                        .gender("Male")
                        .dob(LocalDate.parse("1985-02-10"))
                        .kycType("AADHAAR")
                        .kycDocId("XXXXXXXX2001")
                        .mobileNum("9845000001")
                        .isEarningMember(true)
                        .isNominee(true)
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build(),
                CmFamilyMemberEntity.builder()
                        .familyMemId("FAM100301")
                        .customerId("100301")
                        .applicationId("APP100301")
                        .memberType("SP")
                        .name("Manjunath K")
                        .relation("Spouse")
                        .gender("Male")
                        .dob(LocalDate.parse("1982-05-15"))
                        .kycType("AADHAAR")
                        .kycDocId("XXXXXXXX2002")
                        .mobileNum("9845000002")
                        .isEarningMember(true)
                        .isNominee(true)
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build()
        );
        familyRepo.saveAll(familyMembers);

        logger.info("Successfully populated test seed data in database for Customer Management.");
    }
}
