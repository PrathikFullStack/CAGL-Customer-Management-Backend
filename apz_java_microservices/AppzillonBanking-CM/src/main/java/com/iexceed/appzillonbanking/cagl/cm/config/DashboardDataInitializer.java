package com.iexceed.appzillonbanking.cagl.cm.config;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmCustomerEntity;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmCustomerRepository;

@Component
public class DashboardDataInitializer implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(DashboardDataInitializer.class);

    private final CmApplicationMasterRepository appRepo;
    private final CmCustomerRepository customerRepo;

    public DashboardDataInitializer(CmApplicationMasterRepository appRepo, CmCustomerRepository customerRepo) {
        this.appRepo = appRepo;
        this.customerRepo = customerRepo;
    }

    @Override
    public void run(String... args) {
        try {
            seedMemberIfAbsent("APP289272972921", "289272972921", "Laxmi T", "9876543210",
                    "DRAFT", "KYC Details update", "DRAFT_SAVED", "ONLINE", "NORMAL",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            seedMemberIfAbsent("APP289272972922", "289272972922", "Laxmi T", "9876543210",
                    "BMONHOLD", "Address update", "ONHOLD_BM", "ONLINE", "NORMAL",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            seedMemberIfAbsent("APP289272972923", "289272972923", "Laxmi T", "9876543210",
                    "AMONHOLD", "Personal Details update", "ONHOLD_AM", "ONLINE", "NORMAL",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            seedMemberIfAbsent("APP289272972924", "289272972924", "Laxmi T", "9876543210",
                    "RPCONHOLD", "Bank Details update", "ONHOLD_RPC", "ONLINE", "NORMAL",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            seedMemberIfAbsent("APP289272972925", "289272972925", "Laxmi T", "9876543210",
                    "DRAFT", "KYC Details update", "PENDING", "ONLINE", "CAMPAIGN",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            seedMemberIfAbsent("APP289272972926", "289272972926", "Ramesh Kumar Sharma", "9876543211",
                    "DRAFT", "Family Details update", "DRAFT_SAVED", "OFFLINE", "NORMAL",
                    "GK123456", "Ramesh Kumar", "8282828229", "Kolar", "7828929201", "Old DC Road Pratapr...");

            logger.info("Dashboard sample member seed data successfully verified/initialized.");
        } catch (Exception e) {
            logger.warn("Could not seed dashboard data (table might not exist yet): {}", e.getMessage());
        }
    }

    private void seedMemberIfAbsent(String appId, String custId, String name, String mobile,
                                    String stage, String subStage, String status, String channel, String recordType,
                                    String kmId, String kmName, String kendraId, String kendraName, String groupId, String branchName) {
        if (!appRepo.existsById(appId)) {
            CmApplicationMasterEntity app = CmApplicationMasterEntity.builder()
                    .applicationId(appId)
                    .customerId(custId)
                    .customerName(name)
                    .mobileNumber(mobile)
                    .version("1")
                    .branchId("BR001")
                    .branchName(branchName)
                    .kendraId(kendraId)
                    .kendraName(kendraName)
                    .groupId(groupId)
                    .kmName(kmName)
                    .leader("LDR01")
                    .stage(stage)
                    .subStage(subStage)
                    .wfstage(stage)
                    .status(status)
                    .recordType(recordType)
                    .channelType(channel)
                    .loanEligible("YES")
                    .createdBy(kmId)
                    .createdTs(LocalDateTime.now().minusDays(5))
                    .updatedBy(kmId)
                    .updatedTs(LocalDateTime.now())
                    .build();
            appRepo.save(app);

            if (!customerRepo.existsById(custId)) {
                CmCustomerEntity cust = CmCustomerEntity.builder()
                        .customerId(custId)
                        .applicationId(appId)
                        .customerName(name)
                        .dob("1990-05-15")
                        .maritalStatus("MARRIED")
                        .livePhotoStatus("PASS")
                        .kycStatus("VERIFIED")
                        .primaryKycType("AADHAAR")
                        .primaryKycId("XXXX-XXXX-1234")
                        .createdBy(kmId)
                        .createdTs(LocalDateTime.now().minusDays(5))
                        .build();
                customerRepo.save(cust);
            }
        }
    }
}
