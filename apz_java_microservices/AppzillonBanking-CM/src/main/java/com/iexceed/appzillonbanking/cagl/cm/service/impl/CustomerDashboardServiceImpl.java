package com.iexceed.appzillonbanking.cagl.cm.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmApplicationMasterEntity;
import com.iexceed.appzillonbanking.cagl.cm.payload.dashboard.CmDashboardSummaryDto;
import com.iexceed.appzillonbanking.cagl.cm.repository.primary.CmApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerDashboardService;

@Service
public class CustomerDashboardServiceImpl implements CustomerDashboardService {

    private final CmApplicationMasterRepository appRepo;

    public CustomerDashboardServiceImpl(CmApplicationMasterRepository appRepo) {
        this.appRepo = appRepo;
    }

    @Override
    public CmDashboardSummaryDto getDashboardSummaryAllInOne(
            String role,
            String branchId,
            String search) {

        List<CmApplicationMasterEntity> apps;
        try {
            apps = appRepo.findAll();
        } catch (Exception e) {
            apps = List.of();
        }

        if (branchId != null && !branchId.isBlank()) {
            apps = apps.stream()
                    .filter(a -> branchId.equalsIgnoreCase(a.getBranchId()) || branchId.equalsIgnoreCase(a.getBranchName()))
                    .toList();
        }

        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            apps = apps.stream().filter(a ->
                    (a.getCustomerName() != null && a.getCustomerName().toLowerCase().contains(query)) ||
                    (a.getCustomerId() != null && a.getCustomerId().toLowerCase().contains(query)) ||
                    (a.getApplicationId() != null && a.getApplicationId().toLowerCase().contains(query)) ||
                    (a.getMobileNumber() != null && a.getMobileNumber().contains(query)) ||
                    (a.getKendraName() != null && a.getKendraName().toLowerCase().contains(query)) ||
                    (a.getKmName() != null && a.getKmName().toLowerCase().contains(query))
            ).toList();
        }

        // 1. DRAFTS DATA
        List<CmDashboardSummaryDto.DashboardMemberItemDto> draftsAll = apps.stream()
                .filter(a -> "DRAFT".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> draftsOnline = apps.stream()
                .filter(a -> "DRAFT".equalsIgnoreCase(a.getStage()) && !"OFFLINE".equalsIgnoreCase(a.getChannelType()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> draftsOffline = apps.stream()
                .filter(a -> "DRAFT".equalsIgnoreCase(a.getStage()) && "OFFLINE".equalsIgnoreCase(a.getChannelType()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        // 2. ONHOLD DATA
        List<CmDashboardSummaryDto.DashboardMemberItemDto> onholdAll = apps.stream()
                .filter(a -> a.getStage() != null && a.getStage().toUpperCase().contains("ONHOLD"))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> onholdFromBm = apps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("BMONHOLD"))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> onholdFromAm = apps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("AMONHOLD"))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> onholdFromRpc = apps.stream()
                .filter(a -> a.getStage() != null && a.getStage().equalsIgnoreCase("RPCONHOLD"))
                .map(this::mapToDashboardMemberDto)
                .toList();

        // 3. CAMPAIGN DRIVE DATA
        List<CmDashboardSummaryDto.DashboardMemberItemDto> campaignDrive = apps.stream()
                .filter(a -> "CAMPAIGN".equalsIgnoreCase(a.getRecordType()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        // 4. OVERVIEW DATA
        List<CmDashboardSummaryDto.DashboardMemberItemDto> pendingBmReview = apps.stream()
                .filter(a -> "BMQUEUE".equalsIgnoreCase(a.getStage()) || "BM_REVIEW".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> pendingAmReview = apps.stream()
                .filter(a -> "AMQUEUE".equalsIgnoreCase(a.getStage()) || "AM_REVIEW".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> pendingRpcReview = apps.stream()
                .filter(a -> "RPCMAKERQUEUE".equalsIgnoreCase(a.getStage()) || "RPCCHECKERQUEUE".equalsIgnoreCase(a.getStage()) || "CPU_REVIEW".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> completed = apps.stream()
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> rejected = apps.stream()
                .filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus()) || "REJECTED".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> photoDedupePending = apps.stream()
                .filter(a -> "DEDUPEQUEUE".equalsIgnoreCase(a.getStage()) || "DEDUPE_PENDING".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> t24UpdationPending = apps.stream()
                .filter(a -> "T24PENDING".equalsIgnoreCase(a.getStage()) || "T24_PENDING".equalsIgnoreCase(a.getStage()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        List<CmDashboardSummaryDto.DashboardMemberItemDto> t24UpdationFailed = apps.stream()
                .filter(a -> "T24FAILED".equalsIgnoreCase(a.getStatus()) || "T24_FAILED".equalsIgnoreCase(a.getStatus()))
                .map(this::mapToDashboardMemberDto)
                .toList();

        // 5. ASSEMBLE ALL-IN-ONE RESPONSE
        return CmDashboardSummaryDto.builder()
                .actionRequired(CmDashboardSummaryDto.ActionRequiredDto.builder()
                        .drafts(CmDashboardSummaryDto.DraftsMetricsDto.builder()
                                .total(draftsAll.size())
                                .online(draftsOnline.size())
                                .offline(draftsOffline.size())
                                .build())
                        .onhold(CmDashboardSummaryDto.OnholdMetricsDto.builder()
                                .total(onholdAll.size())
                                .fromBm(onholdFromBm.size())
                                .fromAm(onholdFromAm.size())
                                .fromRpc(onholdFromRpc.size())
                                .build())
                        .campaignDrive(CmDashboardSummaryDto.CampaignMetricsDto.builder()
                                .total(campaignDrive.size())
                                .build())
                        .build())
                .overview(CmDashboardSummaryDto.OverviewDto.builder()
                        .pendingBmReview(pendingBmReview.size())
                        .pendingAmReview(pendingAmReview.size())
                        .pendingRpcReview(pendingRpcReview.size())
                        .completed(completed.size())
                        .rejected(rejected.size())
                        .photoDedupePending(photoDedupePending.size())
                        .t24UpdationPending(t24UpdationPending.size())
                        .t24UpdationFailed(t24UpdationFailed.size())
                        .build())
                .data(CmDashboardSummaryDto.DashboardDataDto.builder()
                        .drafts(CmDashboardSummaryDto.DraftsDataDto.builder()
                                .all(draftsAll)
                                .online(draftsOnline)
                                .offline(draftsOffline)
                                .build())
                        .onhold(CmDashboardSummaryDto.OnholdDataDto.builder()
                                .all(onholdAll)
                                .fromBm(onholdFromBm)
                                .fromAm(onholdFromAm)
                                .fromRpc(onholdFromRpc)
                                .build())
                        .campaignDrive(campaignDrive)
                        .overview(CmDashboardSummaryDto.OverviewDataDto.builder()
                                .pendingBmReview(pendingBmReview)
                                .pendingAmReview(pendingAmReview)
                                .pendingRpcReview(pendingRpcReview)
                                .completed(completed)
                                .rejected(rejected)
                                .photoDedupePending(photoDedupePending)
                                .t24UpdationPending(t24UpdationPending)
                                .t24UpdationFailed(t24UpdationFailed)
                                .build())
                        .build())
                .build();
    }

    private CmDashboardSummaryDto.DashboardMemberItemDto mapToDashboardMemberDto(CmApplicationMasterEntity a) {
        String onholdSource = null;
        if (a.getStage() != null) {
            if (a.getStage().toUpperCase().contains("BM")) onholdSource = "BM";
            else if (a.getStage().toUpperCase().contains("AM")) onholdSource = "AM";
            else if (a.getStage().toUpperCase().contains("RPC")) onholdSource = "RPC";
        }

        String requestType = (a.getSubStage() != null && !a.getSubStage().isBlank())
                ? a.getSubStage()
                : "KYC Details update";

        String channel = ("OFFLINE".equalsIgnoreCase(a.getChannelType())) ? "Offline" : "Online";

        return CmDashboardSummaryDto.DashboardMemberItemDto.builder()
                .applicationId(a.getApplicationId())
                .memberId(a.getCustomerId())
                .memberName(a.getCustomerName())
                .mobileNumber(a.getMobileNumber())
                .kmId(a.getCreatedBy() != null ? a.getCreatedBy() : "GK123456")
                .kmName(a.getKmName() != null ? a.getKmName() : "Ramesh Kumar")
                .kendraId(a.getKendraId() != null ? a.getKendraId() : "8282828229")
                .kendraName(a.getKendraName() != null ? a.getKendraName() : "Kolar")
                .groupId(a.getGroupId() != null ? a.getGroupId() : "7828929201")
                .groupName(a.getGroupId() != null ? "Group " + a.getGroupId() : "Group 1")
                .branchId(a.getBranchId())
                .branchName(a.getBranchName())
                .requestType(requestType)
                .campaignStart("23/08/2026")
                .stage(a.getStage())
                .subStage(a.getSubStage())
                .status(a.getStatus())
                .onholdSource(onholdSource)
                .channel(channel)
                .createdDate(a.getCreatedTs())
                .updatedDate(a.getUpdatedTs())
                .build();
    }
}
