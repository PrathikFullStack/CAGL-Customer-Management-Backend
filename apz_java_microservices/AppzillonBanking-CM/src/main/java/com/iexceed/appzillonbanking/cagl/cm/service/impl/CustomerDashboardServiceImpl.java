package com.iexceed.appzillonbanking.cagl.cm.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.dashboard.CmDashboardSummaryDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerDashboardService;

@Service
public class CustomerDashboardServiceImpl implements CustomerDashboardService {

    private static final Logger logger = LogManager.getLogger(CustomerDashboardServiceImpl.class);

    private final CmApplicationMasterRepository appRepo;

    public CustomerDashboardServiceImpl(CmApplicationMasterRepository appRepo) {
        this.appRepo = appRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public CmDashboardSummaryDto getDashboardSummary(String role, String branchId) {
        logger.info("Generating Dashboard Summary for Role: {}, Branch: {}", role, branchId);

        List<CmApplicationMasterEntity> allApps = appRepo.findAll();

        if (branchId != null && !branchId.isBlank() && !branchId.equalsIgnoreCase("ALL") && !branchId.equalsIgnoreCase("null") && !branchId.equalsIgnoreCase("branchId")) {
            allApps = allApps.stream()
                    .filter(a -> branchId.equalsIgnoreCase(a.getBranchId()) || branchId.equalsIgnoreCase(a.getBranchName()))
                    .collect(Collectors.toList());
        }

        // Action Required Counts
        long drafts = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("DRAFT") ||
                        a.getStage().equalsIgnoreCase("KYC") ||
                        a.getStage().equalsIgnoreCase("INITIATE") ||
                        a.getStage().equalsIgnoreCase("INITIATED")))
                .count();

        long draftsOffline = allApps.stream()
                .filter(a -> ("DRAFT".equalsIgnoreCase(a.getStage()) || "KYC".equalsIgnoreCase(a.getStage())) 
                        && "OFFLINE".equalsIgnoreCase(a.getChannelType()))
                .count();
        long draftsOnline = Math.max(0, drafts - draftsOffline);

        long onhold = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("BMONHOLD") ||
                        a.getStage().equalsIgnoreCase("AMONHOLD") ||
                        a.getStage().equalsIgnoreCase("RPCONHOLD") ||
                        a.getStage().equalsIgnoreCase("ONHOLD")))
                .count();

        long campaignDrive = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("CAMPAIGN") || 
                        "CAMPAIGN".equalsIgnoreCase(a.getRecordType())))
                .count();

        // Overview Counts
        long pendingBmReview = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("BMQUEUE") || 
                        a.getStage().equalsIgnoreCase("BM_REVIEW")))
                .count();

        long pendingAmReview = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("AMQUEUE") || 
                        a.getStage().equalsIgnoreCase("AM_REVIEW")))
                .count();

        long pendingRpcReview = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("RPCMAKERQUEUE") ||
                        a.getStage().equalsIgnoreCase("RPCCHECKERQUEUE") ||
                        a.getStage().equalsIgnoreCase("CPU_REVIEW") ||
                        a.getStage().equalsIgnoreCase("RPC_REVIEW")))
                .count();

        long completed = allApps.stream()
                .filter(a -> a.getStage() != null && (
                        a.getStage().equalsIgnoreCase("COMPLETED") ||
                        a.getStage().equalsIgnoreCase("APPROVED")))
                .count();

        long rejected = allApps.stream()
                .filter(a -> (a.getStage() != null && a.getStage().equalsIgnoreCase("REJECTED")) ||
                             (a.getStatus() != null && a.getStatus().equalsIgnoreCase("REJECTED")))
                .count();

        long photoDedupePending = allApps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("PHOTODEDUPE"))
                .count();

        long t24UpdationPending = allApps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("T24PENDING"))
                .count();

        long t24UpdationFailed = allApps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("T24FAILED"))
                .count();

        return CmDashboardSummaryDto.builder()
                .actionRequired(CmDashboardSummaryDto.ActionRequiredCounts.builder()
                        .drafts(drafts)
                        .draftsOnline(draftsOnline)
                        .draftsOffline(draftsOffline)
                        .onhold(onhold)
                        .campaignDrive(campaignDrive)
                        .build())
                .overview(CmDashboardSummaryDto.OverviewCounts.builder()
                        .pendingBmReview(pendingBmReview)
                        .pendingAmReview(pendingAmReview)
                        .pendingRpcReview(pendingRpcReview)
                        .completed(completed)
                        .rejected(rejected)
                        .photoDedupePending(photoDedupePending)
                        .t24UpdationPending(t24UpdationPending)
                        .t24UpdationFailed(t24UpdationFailed)
                        .build())
                .build();
    }
}
