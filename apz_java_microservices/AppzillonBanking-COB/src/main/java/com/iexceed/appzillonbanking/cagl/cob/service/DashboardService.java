package com.iexceed.appzillonbanking.cagl.cob.service;
import com.iexceed.appzillonbanking.cagl.cob.constants.AuditConstants;
import com.iexceed.appzillonbanking.cagl.cob.constants.DashboardConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.domain.spec.GroupSpecification;
import com.iexceed.appzillonbanking.cagl.cob.domain.spec.KendraSpecification;
import com.iexceed.appzillonbanking.cagl.cob.domain.spec.OnboardingSpecification;
import com.iexceed.appzillonbanking.cagl.cob.mapper.ApplicationSummaryMapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.OnboardingRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLeadRepository;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final OnboardingRepository onboardingRepository;
    private  final AuditService auditService;
    private final TbObKendraRepository kendraRepository;
    private final TbObGroupRepository groupRepository;
    private final TbObLeadRepository leadRepository;
    private final ApplicationSummaryMapper applicationSummaryMapper;



    @Value("${cob.dashboard.tablet.page-size:10}")
    private int defaultPageSize;

    @Value("${dashboard.list.default-sort-by:createdTs}")
    private String defaultSortBy;

    @Value("${dashboard.list.default-sort-order:ASC}")
    private String defaultSortOrder;

    @Value("${cob.dashboard.rpc.green-channel-age-days:2}")
    private int greenChannelAgeDays;

    public DashboardResponse getDashboardCounts(DashboardRequest request, Header header) {
        DashboardRequestFields fields = request.getReqObj();
        if (fields == null) {
            return new DashboardResponse(new ArrayList<>());
        }
        String userRole = fields.getUserRole();
        if ("KM".equalsIgnoreCase(userRole) || "DEO".equalsIgnoreCase(userRole)) {
            return getKmDashboardCounts(fields.getKendraIds(), fields.getGroupIds(), fields.getUserId());
        }
        if ("BM".equalsIgnoreCase(userRole)) {
            return getBmDashboardCounts(fields.getBranchIds());
        }
        if ("AM".equalsIgnoreCase(userRole)) {
            return getAmDashboardCounts(fields.getBranchId());
        }
        if ("CHT".equalsIgnoreCase(userRole)) {
            return getChtDashboardCounts();
        }
        if (isRpcAmlHoRole(userRole)) {
            return getRpcAmlHoDashboardCounts(fields.getBranchIds());
        }
        if (isRpcTlRole(userRole)) {
            return getRpcTlInHoDashboardCounts(fields.getBranchIds());
        }
        if (isRpcRole(userRole)) {
            return getRpcDashboardCounts(fields.getBranchIds(), fields.getUserId());
        }
        if ("CRT".equalsIgnoreCase(userRole)) {
            return getCrtDashboardCounts();
        }
        return new DashboardResponse(new ArrayList<>());
    }

    private DashboardResponse getKmDashboardCounts(List<String> kendraIds, List<String> groupIds, String userId) {
        List<TileCount> tiles = new ArrayList<>();
        long drafts = onboardingRepository.countByStageAndKendraIdIn("DRAFT", kendraIds);
        long cbFail = onboardingRepository.countByStageInAndKendraIdIn(List.of("BREQUEUE", "CRTQUEUE"), kendraIds);
        long pendingWithRpc = onboardingRepository.countByStageAndKendraIdIn("RPCQUEUE", kendraIds);
        long inputLoanDetails = onboardingRepository.countByStageAndRecordTypeInAndKendraIdIn("LOAN", List.of("REPLACEMENT", "NEW"), kendraIds);
        long rpcOnHold = onboardingRepository.countByStageAndWfStageAndKendraIdIn("ONHOLD", "RPCONHOLD", kendraIds);
        long cgt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForGroups(groupIds))
                .and(OnboardingSpecification.stageIn(List.of("CGT", "RPCQUEUE"))));
        long reinterview = onboardingRepository.countReinterviewByRecordTypeInAndKendraIdIn(List.of("REPLACEMENT", "NEW"), kendraIds);
        long pendingForGrt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingGrtGroupIds(kendraIds))
                .and(OnboardingSpecification.stageEquals("GRT")));

        tiles.add(new TileCount(DashboardConstants.TILE_DRAFTS_MBDF, drafts));
        tiles.add(new TileCount("BRE_FAIL", cbFail));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_WITH_RPC, pendingWithRpc));
        tiles.add(new TileCount(DashboardConstants.TILE_INPUT_LOAN_DETAILS, inputLoanDetails));
        tiles.add(new TileCount(DashboardConstants.TILE_ONHOLD, rpcOnHold));
        tiles.add(new TileCount(DashboardConstants.TILE_CGT, cgt));
        tiles.add(new TileCount("RE_INTERVIEW", reinterview));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_GRT, pendingForGrt));
        long totalPendingMbdfs = drafts + cbFail + pendingWithRpc + inputLoanDetails + rpcOnHold + cgt + reinterview + pendingForGrt;
        tiles.add(new TileCount("Total Pending MBDFs", totalPendingMbdfs));
        tiles.add(new TileCount(DashboardConstants.TILE_REJECTED, onboardingRepository.countRejectedByKendraIdIn(kendraIds, DashboardConstants.REJECT_STAGES)));
        tiles.add(new TileCount(DashboardConstants.TILE_ACTIVATED, onboardingRepository.countActivatedByKendraIdIn(kendraIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_REACTIVATION, onboardingRepository.countByRecordTypeAndKendraIdIn(DashboardConstants.RECORD_TYPE_REACTIVATION, kendraIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_NEW_KENDRAS, kendraRepository.countByStatusAndKendraIdIn("ACTIVE",kendraIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_NEW_GROUPS, groupRepository.countByStatusAndKendraIdIn("ACTIVE", kendraIds)));
        return new DashboardResponse(tiles);
    }

    private DashboardResponse getBmDashboardCounts(List<String> branchIds) {
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount(DashboardConstants.TILE_LEADS, leadRepository.countByBranchIdIn(branchIds)));

        long newKendras = kendraRepository.countByStatusAndBranchIdIn("PENDING", branchIds)
                + kendraRepository.countByStatusAndBranchIdIn("ACTIVE", branchIds);
        long newGroups = groupRepository.countByStatusAndBranchIdIn("PENDING", branchIds)
                + groupRepository.countByStatusAndBranchIdIn("ACTIVE", branchIds);
        tiles.add(new TileCount(DashboardConstants.TILE_NEW_KENDRAS_GROUPS, newKendras + newGroups));

        long drafts = onboardingRepository.countByStageAndBranchIdIn("DRAFT", branchIds);
        long cbFail = onboardingRepository.countByStageInAndBranchIdIn(List.of("BREQUEUE", "CRTQUEUE"), branchIds);
        long pendingWithRpc = onboardingRepository.countByStageAndBranchIdIn("RPCQUEUE", branchIds);

        long inputLoanDetails = onboardingRepository.countByStageAndRecordTypeInAndBranchIdIn("LOAN", List.of("REPLACEMENT", "NEW"), branchIds);

        long rpcOnHold = onboardingRepository.countByStageAndWfStageAndBranchIdIn("ONHOLD", "RPCONHOLD", branchIds);


        long cgt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForBranches(branchIds))
                .and(OnboardingSpecification.stageIn(List.of("CGT", "RPCQUEUE"))));


        long reinterview = onboardingRepository.countByStageAndRecordTypeInAndBranchIdIn("BMQUEUE", List.of("REPLACEMENT", "NEW"), branchIds);

        long pendingForGrt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingGrtGroupIdsForBranches(branchIds))
                .and(OnboardingSpecification.stageEquals("GRT")));

        tiles.add(new TileCount(DashboardConstants.TILE_DRAFTS_MBDF, drafts));
        tiles.add(new TileCount("BRE_FAIL", cbFail));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_WITH_RPC, pendingWithRpc));
        tiles.add(new TileCount(DashboardConstants.TILE_INPUT_LOAN_DETAILS, inputLoanDetails));
        tiles.add(new TileCount(DashboardConstants.TILE_ONHOLD, rpcOnHold));
        tiles.add(new TileCount(DashboardConstants.TILE_CGT, cgt));
        tiles.add(new TileCount("REINTERVIEW", reinterview));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_GRT, pendingForGrt));
        long totalPendingMbdfs = drafts + cbFail + pendingWithRpc + inputLoanDetails + rpcOnHold + cgt + reinterview + pendingForGrt;
        tiles.add(new TileCount("Total Pending MBDFs", totalPendingMbdfs));

        tiles.add(new TileCount(DashboardConstants.TILE_ACTIVATED_REJECTED,
                onboardingRepository.countActivatedOrRejectedByBranchIdIn(branchIds, DashboardConstants.REJECT_STAGES)));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_ACTIVATION,
                onboardingRepository.countPendingForActivationByBranchIdIn(branchIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_TRANSFER_MBDF, 0));
        tiles.add(new TileCount(DashboardConstants.TILE_ASSIGN_ALLOCATE_KENDRAS, 0));

        return new DashboardResponse(tiles);
    }


    private DashboardResponse getAmDashboardCounts(String branchId) {
        List<TileCount> tiles = new ArrayList<>();

        tiles.add(new TileCount(DashboardConstants.TILE_LEADS, leadRepository.countByBranchId(branchId)));

        long newKendras = kendraRepository.countByStatusAndBranchId("PENDING", branchId)
                + kendraRepository.countByStatusAndBranchId("ACTIVE", branchId);
        long newGroups = groupRepository.countByStatusAndBranchId("PENDING", branchId)
                + groupRepository.countByStatusAndBranchId("ACTIVE", branchId);
        tiles.add(new TileCount(DashboardConstants.TILE_NEW_KENDRAS_GROUPS, newKendras + newGroups));

        long drafts = onboardingRepository.countByStageAndBranchId("DRAFT", branchId);
        long breFail = onboardingRepository.countByStageInAndBranchId(List.of("BREQUEUE", "CRTQUEUE"), branchId);
         long pendingWithRpc = onboardingRepository.countByStageAndBranchId("RPCQUEUE", branchId);
        long inputLoanDetails = onboardingRepository.countByStageAndRecordTypeInAndBranchId("LOAN", List.of("REPLACEMENT", "NEW"), branchId);
        long onhold = onboardingRepository.countByStageAndWfStageAndBranchId("ONHOLD", "RPCONHOLD", branchId);
        long cgt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForBranch(branchId)));
        long reinterview = onboardingRepository.countByStageAndRecordTypeInAndBranchId("BMQUEUE", List.of("REPLACEMENT", "NEW"), branchId);
        long pendingForGrt = onboardingRepository.count(OnboardingSpecification.groupIdIn(getPendingGrtGroupIdsForBranch(branchId)));

        tiles.add(new TileCount(DashboardConstants.TILE_DRAFTS_MBDF, drafts));
        tiles.add(new TileCount("BRE_FAIL", breFail));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_WITH_RPC, pendingWithRpc));
        tiles.add(new TileCount(DashboardConstants.TILE_INPUT_LOAN_DETAILS, inputLoanDetails));
        tiles.add(new TileCount(DashboardConstants.TILE_ONHOLD, onhold));
        tiles.add(new TileCount(DashboardConstants.TILE_CGT, cgt));
        tiles.add(new TileCount("RE_INTERVIEW", reinterview));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_GRT, pendingForGrt));
        long totalPendingMbdfs = drafts + breFail + pendingWithRpc + inputLoanDetails + onhold + cgt + reinterview + pendingForGrt;
        tiles.add(new TileCount("Total Pending MBDFs", totalPendingMbdfs));

        // Activated: wfStage="T24_ACTIVATED" (status is never set to this value). Rejected:
        // stage IN REJECT_STAGES OR status="REJECTED" — see findActivatedOrRejectedByBranchId's comment.
        long activatedAndRejected = onboardingRepository.countActivatedOrRejectedByBranchId(branchId, DashboardConstants.REJECT_STAGES);
        tiles.add(new TileCount(DashboardConstants.TILE_ACTIVATED_REJECTED, activatedAndRejected));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_ACTIVATION, onboardingRepository.countPendingForActivationByBranchId(branchId)));

        // Queries pending — to be filled in per tile.
        tiles.add(new TileCount(DashboardConstants.TILE_TRANSFER_MBDF, 0));
        tiles.add(new TileCount(DashboardConstants.TILE_ASSIGN_ALLOCATE_KENDRAS, 0));
        tiles.add(new TileCount(DashboardConstants.TILE_EXCEPTIONAL_APPROVAL, 0));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_T24_GROUPS, 0));

        return new DashboardResponse(tiles);
    }

    private DashboardResponse getChtDashboardCounts() {
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_T24_KENDRAS, kendraRepository.countPendingForT24Activation()));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_T24_GROUPS, groupRepository.countPendingForT24Activation()));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_T24_MEMBER, onboardingRepository.countPendingForActivation()));
        return new DashboardResponse(tiles);
    }

    private boolean isRpcRole(String userRole) {
        return DashboardConstants.isRpcRole(userRole);
    }

    private boolean isRpcTlRole(String userRole) {
        return DashboardConstants.isRpcTlRole(userRole);
    }


    private boolean isRpcAmlHoRole(String userRole) {
        return DashboardConstants.isRpcAmlHoRole(userRole);
    }

    /**
     * CGT tile: a group counts as pending CGT via tb_ob_group.cgt_status, not the application's own
     * kendra (KM), branches (BM), or branch (AM).
     */
    private List<String> getPendingCgtGroupIds(List<String> kendraIds) {
        return groupRepository.findByKendraIdInAndCgtStatus(kendraIds, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }


    private List<String> getPendingCgtGroupIdsForGroups(List<String> groupIds) {
        return groupRepository.findByGroupIdInAndCgtStatus(groupIds, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

    private List<String> getPendingCgtGroupIdsForBranches(List<String> branchIds) {
        return groupRepository.findByBranchIdInAndCgtStatus(branchIds, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

    private List<String> getPendingCgtGroupIdsForBranch(String branchId) {
        return groupRepository.findByBranchIdAndCgtStatus(branchId, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

    /**
     * PENDING_FOR_GRT tile: a group counts as pending GRT via tb_ob_group.grt_status, mirroring the
     * CGT tile's cgt_status-based scoping above.
     */
    private List<String> getPendingGrtGroupIds(List<String> kendraIds) {
        return groupRepository.findByKendraIdInAndGrtStatus(kendraIds, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

    private List<String> getPendingGrtGroupIdsForBranches(List<String> branchIds) {
        return groupRepository.findByBranchIdInAndGrtStatus(branchIds, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

    private List<String> getPendingGrtGroupIdsForBranch(String branchId) {
        return groupRepository.findByBranchIdAndGrtStatus(branchId, "PENDING").stream()
                .map(TbObGroup::getGroupId)
                .collect(Collectors.toList());
    }

      private DashboardResponse getRpcDashboardCounts(List<String> branchIds, String userId) {
        List<TileCount> tiles = new ArrayList<>();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long rpcOnHold = onboardingRepository.countByStageAndWfStageAndBranchIdIn("ONHOLD", "RPCONHOLD", branchIds);
        tiles.add(new TileCount("Maker’s Pool - Pending", onboardingRepository.countByStageAndWfStageAndBranchIdIn("RPCQUEUE", "RPC_QUEUE", branchIds)));
        tiles.add(new TileCount("Maker’s Pool - Onhold", rpcOnHold));
        tiles.add(new TileCount("Checker’s Pool - Pending", onboardingRepository.countByStageAndWfStageAndBranchIdIn("RPCQUEUE", "RPCCHECKERREVIEW", branchIds)));
        tiles.add(new TileCount("Checker’s Pool - Onhold", rpcOnHold));
        long clearedToday = onboardingRepository.countByUpdatedByAndUpdatedTsAfter(userId, startOfDay);
        tiles.add(new TileCount("Cleared by user - Maker", clearedToday));
        tiles.add(new TileCount("Cleared by user - Checker", clearedToday));
        return new DashboardResponse(tiles);
    }


    private DashboardResponse getRpcTlInHoDashboardCounts(List<String> branchIds) {
        List<TileCount> tiles = new ArrayList<>();
        long rpcOnHold = onboardingRepository.countByStageAndWfStageAndBranchIdIn("ONHOLD", "RPCONHOLD", branchIds);
        tiles.add(new TileCount("Maker’s Pool - Pending", onboardingRepository.countByStageAndWfStageAndBranchIdIn("RPCQUEUE", "RPCQUEUE", branchIds)));
        tiles.add(new TileCount("Maker’s Pool - Onhold", rpcOnHold));
        tiles.add(new TileCount("Checker’s Pool - Pending", onboardingRepository.countByStageAndWfStageAndBranchIdIn("RPCQUEUE", "RPCCHECKERREVIEW", branchIds)));
        tiles.add(new TileCount("Checker’s Pool - Onhold", rpcOnHold));
        tiles.add(new TileCount("Cleared Cases", onboardingRepository.countByStageInAndBranchIdIn(DashboardConstants.RPC_CLEARED_STAGES, branchIds)));
        LocalDateTime greenChannelSince = LocalDateTime.now().minusDays(greenChannelAgeDays);
        tiles.add(new TileCount(DashboardConstants.TILE_GREEN_CHANNEL, onboardingRepository.countByChannelTypeAndBranchIdInAndCreatedTsAfter(DashboardConstants.CHANNEL_TYPE_GREEN, branchIds, greenChannelSince)));
        return new DashboardResponse(tiles);
    }
 private DashboardResponse getRpcAmlHoDashboardCounts(List<String> branchIds) {
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount("Pending", onboardingRepository.countByStageAndBranchIdIn("AMLQUEUE", branchIds)));
        tiles.add(new TileCount("Approved", onboardingRepository.countByStageAndBranchIdIn("BREQUEUE", branchIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_REJECTED, onboardingRepository.countByStageAndBranchIdIn("AMLREJECTED", branchIds)));
        return new DashboardResponse(tiles);
    }



    private DashboardResponse getCrtDashboardCounts() {
        long pendingCount = onboardingRepository.countByStage("CRTQUEUE");
        long approvedCount = onboardingRepository.countByStageIn(DashboardConstants.CRT_APPROVED_STAGES);
        long rejectedCount = onboardingRepository.countByStage("CRTREJECTED");
        long crtUserPoolCount = pendingCount + approvedCount + rejectedCount;

        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount("CRT User Pool", crtUserPoolCount));
        tiles.add(new TileCount("Pending", pendingCount));
        tiles.add(new TileCount("Approved", approvedCount));
        tiles.add(new TileCount(DashboardConstants.TILE_REJECTED, rejectedCount));
        return new DashboardResponse(tiles);
    }

    public ResponseWrapper getDashboardList(DashboardListRequest request,Header header) {
        DashboardListRequestFields fields = request.getReqObj();
        if (fields == null) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            DashboardListResponseObj emptyResponseObj = new DashboardListResponseObj(Page.empty(), new ArrayList<>());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                ResponseBody responseBody = new ResponseBody();
                responseBody.setResponseObj(objectMapper.writeValueAsString(emptyResponseObj));
                ResponseHeader responseHeader = new ResponseHeader();
                CommonUtils.generateHeaderForSuccess(responseHeader);
                responseWrapper.setApiResponse(
                        Response.builder()
                                .responseHeader(responseHeader)
                                .responseBody(responseBody)
                                .build()
                );      } catch (Exception e) {
                log.error("Error serializing empty response", e);
            }
            return responseWrapper;
        }
        log.info("DashboardListRequest initiated for userId: {}, userRole: {}, tileType: {}", fields.getUserId(), fields.getUserRole(), fields.getTileType());
        String eventType = fields.getSearchType() != null && !fields.getSearchType().isBlank()
                ? auditService.resolveSearchEventType(fields.getSearchType())
                : "OnboardingDashboardList";
        auditService.saveUserAudit(eventType, null, null, fields.getUserId(), null,
                fields.getUserRole(), fields.getBranchId(), null, null, fields);
        DashboardRequest countRequest = new DashboardRequest();
        DashboardRequestFields countRequestFields = new DashboardRequestFields();
        countRequestFields.setUserId(fields.getUserId());
        countRequestFields.setUserRole(fields.getUserRole());
        countRequestFields.setBranchId(fields.getBranchId());
        countRequestFields.setKendraIds(fields.getKendraIds());
        countRequestFields.setGroupIds(fields.getGroupIds());
        countRequestFields.setBranchIds(fields.getBranchIds());
        countRequest.setReqObj(countRequestFields);

        DashboardResponse countResponse = getDashboardCounts(countRequest, header);
        List<TileCount> tiles = countResponse.getTiles();
        log.info("Dashboard counts fetched for userId: {}: {}", fields.getUserId(), tiles);

        PaginationRequest pagination = fields.getPagination() != null ? fields.getPagination() : new PaginationRequest();
        int page = pagination.getPageNo() != null ? pagination.getPageNo() : 0;
        int size = pagination.getPageSize() != null ? pagination.getPageSize() : defaultPageSize;
        String sortBy = pagination.getSortBy() != null ? pagination.getSortBy() : defaultSortBy;
        String sortOrder = pagination.getSortOrder() != null ? pagination.getSortOrder() : defaultSortOrder;

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<?> resultPage;
        String userRole = fields.getUserRole();
        if ("GLOBAL".equalsIgnoreCase(fields.getSearchType())) {
            log.info("Routing to getGlobalSearchList for userId: {}, userRole: {}", fields.getUserId(), userRole);
            resultPage = getGlobalSearchList(fields, pageable);
        } else if ("BM".equalsIgnoreCase(userRole)) {
            log.info("Routing to getBmDashboardList for branchIds: {}", fields.getBranchIds());
            resultPage = getBmDashboardList(fields, pageable);
        } else if ("AM".equalsIgnoreCase(userRole)) {
            log.info("Routing to getAmDashboardList for branchId: {}", fields.getBranchId());
            resultPage = getAmDashboardList(fields, pageable);
        } else if ("CHT".equalsIgnoreCase(userRole)) {
            log.info("Routing to getChtDashboardList for userId: {}", fields.getUserId());
            resultPage = getChtDashboardList(fields, pageable);
        } else if (isRpcAmlHoRole(userRole)) {
            log.info("Routing to getRpcAmlHoDashboardList for userId: {}", fields.getUserId());
            resultPage = getRpcAmlHoDashboardList(fields, pageable);
        } else if (isRpcTlRole(userRole)) {
            log.info("Routing to getRpcTlInHoDashboardList (RPC TL/IN/HO) for userId: {}", fields.getUserId());
            resultPage = getRpcTlInHoDashboardList(fields, pageable);
        } else if (isRpcRole(userRole)) {
            log.info("Routing to getRpcDashboardList for userId: {}", fields.getUserId());
            resultPage = getRpcDashboardList(fields, pageable);
        } else if ("CRT".equalsIgnoreCase(userRole)) {
            log.info("Calling getCrtDashboardList for userId: {}", fields.getUserId());
            resultPage = getCrtDashboardList(fields, pageable);
        } else {
            log.info("Routing to getKmDashboardList for userId: {}", fields.getUserId());
            resultPage = getKmDashboardList(fields, pageable);
        }
        DashboardListResponseObj responseObj = new DashboardListResponseObj(resultPage, tiles);
        if ("CRT".equalsIgnoreCase(userRole)) {
            responseObj.setCaseAgeingList(getCrtCaseAgeingCounts(fields));
        } else if (isRpcRole(userRole)) {
            responseObj.setCaseAgeingList(getRpcCaseAgeingCounts(fields));
        }
        if (resultPage.isEmpty()) {
            String message = "No records found for userId: " + fields.getUserId() + " with tileType: " + fields.getTileType();
            log.warn(message + ". Returning empty appList.");
        } else {
            log.info("Found {} records for userId: {} with tileType: {}.", resultPage.getTotalElements(), fields.getUserId(), fields.getTileType());
        }
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ResponseBody responseBody = new ResponseBody();
            responseBody.setResponseObj(
                    objectMapper.writeValueAsString(responseObj)
            );
            ResponseHeader responseHeader = new ResponseHeader();
            CommonUtils.generateHeaderForSuccess(responseHeader);
            responseWrapper.setApiResponse(
                    Response.builder()
                            .responseHeader(responseHeader)
                            .responseBody(responseBody)
                            .build()
            );

        } catch (Exception e) {
            log.error("Error serializing response", e);
        }

        return responseWrapper;
    }

    private Page<ApplicationSummary> getGlobalSearchList(DashboardListRequestFields fields, Pageable pageable) {
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.buildRoleScopeSpecification(fields)
                .and(OnboardingSpecification.buildGlobalSearchSpecification(fields));
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<?> getKmDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        List<String> kendraIds = fields.getKendraIds();
           return switch (fields.getTileType()) {
            case DashboardConstants.TILE_DRAFTS_MBDF -> getDraftTileList(fields, pageable);
            case "BRE_FAIL" -> getCbFailTileList(fields, pageable);
            case DashboardConstants.TILE_PENDING_WITH_RPC -> getRpcQueueTileList(fields, pageable);
            case DashboardConstants.TILE_CGT -> getCgtTileList(fields, pageable);
            case DashboardConstants.TILE_ACTIVATED_REJECTED -> getActivatedRejectedTileList(fields, pageable);
            case DashboardConstants.TILE_INPUT_LOAN_DETAILS -> getInputLoanDetailsTileList(fields, pageable);
            case DashboardConstants.TILE_ONHOLD -> getOnholdTileList(fields, pageable);
            case "RE_INTERVIEW" -> getReinterviewTileList(fields, pageable);
            case DashboardConstants.TILE_PENDING_FOR_ACTIVATION -> onboardingRepository.findAll(OnboardingSpecification.grtStyle("GRTAPPROVED")
                            .and(OnboardingSpecification.kendraIdIn(kendraIds))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case DashboardConstants.TILE_PENDING_FOR_GRT -> onboardingRepository.findAll(OnboardingSpecification.groupIdIn(getPendingGrtGroupIds(kendraIds))
                            .and(OnboardingSpecification.stageEquals("GRT"))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case DashboardConstants.TILE_NEW_KENDRAS_GROUPS -> getNewKendraAndGroupTileList(fields, pageable);
            default -> Page.empty();
        };
    }

    private Page<?> getBmDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        return switch (fields.getTileType()) {
            case DashboardConstants.TILE_NEW_KENDRAS_GROUPS -> getNewKendraAndGroupTileListForBm(fields, pageable);
            case DashboardConstants.TILE_DRAFTS_MBDF -> getDraftTileListForBm(fields, pageable);
            case "BRE_FAIL" -> getCbFailTileListForBm(fields, pageable);
            case DashboardConstants.TILE_PENDING_WITH_RPC -> getRpcQueueTileListForBm(fields, pageable);
            case DashboardConstants.TILE_INPUT_LOAN_DETAILS -> getInputLoanDetailsTileListForBm(fields, pageable);
            case DashboardConstants.TILE_ONHOLD -> getOnholdTileListForBm(fields, pageable);
            case DashboardConstants.TILE_CGT -> getCgtTileListForBm(fields, pageable);
            case "REINTERVIEW" -> getReinterviewTileListForBm(fields, pageable);
            case DashboardConstants.TILE_PENDING_FOR_GRT -> onboardingRepository.findAll(OnboardingSpecification.groupIdIn(getPendingGrtGroupIdsForBranches(fields.getBranchIds()))
                            .and(OnboardingSpecification.stageEquals("GRT"))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case DashboardConstants.TILE_ACTIVATED_REJECTED -> getActivatedRejectedTileListForBm(fields, pageable);
            case DashboardConstants.TILE_PENDING_FOR_ACTIVATION -> onboardingRepository.findAll(OnboardingSpecification.grtStyle("GRTAPPROVED")
                            .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case DashboardConstants.TILE_TRANSFER_MBDF -> getTransferMbdfTileListForBm(fields, pageable);
            case DashboardConstants.TILE_ASSIGN_ALLOCATE_KENDRAS -> getAssignAllocateKendrasTileListForBm(fields, pageable);
            default -> Page.empty();
        };
    }

    private Page<?> getLeadsTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            case "NEW_CUSTOMER_LEADS", "UNNATI", "MAHI", "IGL_RENEW", "PR_LEADS", "DROPOUT_CUSTOMERS",
                 "WHATSAPP_AND_OTHER_CHANNELS", "ALL" -> Page.empty();
            default -> Page.empty();
        };
    }

    private Page<?> getTransferMbdfTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            case "ALL" -> Page.empty();
            default -> Page.empty();
        };
    }

    private Page<?> getAssignAllocateKendrasTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            case "ALLOCATE_KENDRAS", "ASSIGN_KENDRAS", "ALL" -> Page.empty();
            default -> Page.empty();
        };
    }


    private Page<?> getAmDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        return switch (fields.getTileType()) {
            case DashboardConstants.TILE_NEW_KENDRAS_GROUPS -> getNewKendrasGroupsTileListForAm(fields, pageable);
            case DashboardConstants.TILE_DRAFTS_MBDF -> getDraftsMbdfTileListForAm(fields, pageable);
            case "BRE_FAIL" -> getBreFailTileListForAm(fields, pageable);
            case DashboardConstants.TILE_PENDING_WITH_RPC -> getPendingWithRpcTileListForAm(fields, pageable);
            case DashboardConstants.TILE_INPUT_LOAN_DETAILS -> getInputLoanDetailsTileListForAm(fields, pageable);
            case DashboardConstants.TILE_ONHOLD -> getOnholdTileListForAm(fields, pageable);
            case DashboardConstants.TILE_CGT -> getCgtTileListForAm(fields, pageable);
            case "RE_INTERVIEW" -> getReinterviewTileListForAm(fields, pageable);
            case DashboardConstants.TILE_PENDING_FOR_GRT -> getPendingForGrtTileListForAm(fields, pageable);
            case DashboardConstants.TILE_ACTIVATED_REJECTED -> getActivatedRejectedTileListForAm(fields, pageable);
            case DashboardConstants.TILE_PENDING_FOR_ACTIVATION -> getPendingForActivationTileListForAm(fields, pageable);
            case DashboardConstants.TILE_TRANSFER_MBDF -> getTransferMbdfTileListForAm(fields, pageable);
            case DashboardConstants.TILE_ASSIGN_ALLOCATE_KENDRAS -> getAssignAllocateKendrasTileListForAm(fields, pageable);
            case DashboardConstants.TILE_EXCEPTIONAL_APPROVAL -> getExceptionalApprovalTileListForAm(fields, pageable);
            case DashboardConstants.TILE_PENDING_T24_GROUPS -> getPendingForT24ActivationGroupsTileListForAm(fields, pageable);
            default -> Page.empty();
        };
    }

    private Page<KendraSummary> getNewKendrasGroupsTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObKendra> statusSpec = switch (subCategory) {
            case "PENDING_FOR_ACTIVATION" -> KendraSpecification.statusEquals("PENDING");
            case "ACTIVE" -> KendraSpecification.statusEquals("ACTIVE");
            default -> KendraSpecification.statusIn(List.of("PENDING", "ACTIVE"));
        };
        Page<TbObKendra> kendras = kendraRepository.findAll(statusSpec
                .and(KendraSpecification.branchIdEquals(fields.getBranchId()))
                .and(KendraSpecification.buildSearchSpecification(fields)), pageable);
        return kendras.map(this::convertToKendraSummary);
    }

    private Page<ApplicationSummary> getDraftsMbdfTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec;
        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION" -> stageSpec = OnboardingSpecification.stageEquals("DRAFT");
            case "CB_QUEUE" -> stageSpec = OnboardingSpecification.stageEquals("BREQUEUE");
            case "UNNATI_PENDING" -> stageSpec = OnboardingSpecification.stageEquals("OPENMARKET");
            case "PHOTO_DEDUPE_FAIL" -> {
                return Page.empty();
            }
            default -> stageSpec = OnboardingSpecification.stageIn(List.of("DRAFT", "BREQUEUE", "OPENMARKET"));
        }
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getBreFailTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec = switch (subCategory) {
            case "PENDING_WITH_KM" -> OnboardingSpecification.stageEquals("BREQUEUE");
            case "PENDING_WITH_CRT" -> OnboardingSpecification.stageEquals("CRTQUEUE");
            default -> OnboardingSpecification.stageIn(List.of("BREQUEUE", "CRTQUEUE"));
        };
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getPendingWithRpcTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> spec = switch (subCategory) {
            case "PENDING_WITH_MAKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPCQUEUE"));
            case "PENDING_WITH_CHECKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPCCHECKERREVIEW"));
            default -> OnboardingSpecification.stageEquals("RPCQUEUE");
        };
        return onboardingRepository.findAll(spec
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getInputLoanDetailsTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("LOAN")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getOnholdTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("ONHOLD")
                        .and(OnboardingSpecification.wfStageEquals("RPCONHOLD"))
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCgtTileList(DashboardListRequestFields fields, Pageable pageable) {
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForGroups(fields.getGroupIds()))
                .and(OnboardingSpecification.stageIn(List.of("CGT", "RPCQUEUE")))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCgtTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForBranches(fields.getBranchIds()))
                .and(OnboardingSpecification.stageIn(List.of("CGT", "RPCQUEUE")))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCgtTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.groupIdIn(getPendingCgtGroupIdsForBranch(fields.getBranchId()))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getReinterviewTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("BMQUEUE")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getPendingForGrtTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        return onboardingRepository.findAll(OnboardingSpecification.groupIdIn(getPendingGrtGroupIdsForBranch(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getActivatedRejectedTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        String branchId = fields.getBranchId();
        Specification<TbObApplicationMaster> statusSpec = switch (subCategory) {
            case "ACTIVATED" -> OnboardingSpecification.activatedSpec();
            case "REJECTED" -> OnboardingSpecification.rejectedSpec(DashboardConstants.REJECT_STAGES);
            default -> OnboardingSpecification.activatedOrRejectedSpec(DashboardConstants.REJECT_STAGES);
        };
        return onboardingRepository.findAll(OnboardingSpecification.branchIdEquals(branchId)
                        .and(statusSpec)
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }
   private Page<ApplicationSummary> getPendingForActivationTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        return onboardingRepository.findAll(OnboardingSpecification.grtStyle("GRTAPPROVED")
                        .and(OnboardingSpecification.branchIdEquals(fields.getBranchId()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<?> getLeadsTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            default -> Page.empty();
        };
    }


    private Page<?> getTransferMbdfTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            default -> Page.empty();
        };
    }

    private Page<?> getAssignAllocateKendrasTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            default -> Page.empty();
        };
    }


    private Page<?> getExceptionalApprovalTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            default -> Page.empty();
        };
    }


    private Page<?> getPendingForT24ActivationGroupsTileListForAm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            default -> Page.empty();
        };
    }

    private Page<?> getChtDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        return switch (fields.getTileType()) {
            case DashboardConstants.TILE_PENDING_T24_KENDRAS -> kendraRepository.findAll(KendraSpecification.statusEquals("ACTIVE")
                            .and(KendraSpecification.t24RefNoIsNull())
                            .and(KendraSpecification.buildSearchSpecification(fields)), pageable)
                    .map(this::convertToKendraSummary);
            case DashboardConstants.TILE_PENDING_T24_GROUPS -> groupRepository.findAll(GroupSpecification.statusEquals("ACTIVE")
                            .and(GroupSpecification.t24RefNoIsNull())
                            .and(GroupSpecification.buildSearchSpecification(fields)), pageable)
                    .map(this::convertGroupToKendraSummary);
            case DashboardConstants.TILE_PENDING_T24_MEMBER -> onboardingRepository.findAll(OnboardingSpecification.grtStyle("GRTAPPROVED")
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            default -> Page.empty();
        };
    }
    private Page<?> getRpcDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        if (DashboardConstants.FLOW_DEFAULT.equalsIgnoreCase(fields.getFlow())) {
            log.info("RPC default flow for userId: {} - returning counts only, no list", fields.getUserId());
            return Page.empty(pageable);
        }
        return switch (fields.getTileType()) {
            case DashboardConstants.TILE_MAKERS_POOL -> getRpcMakerPoolList(fields, pageable);
            case DashboardConstants.TILE_CHECKERS_POOL -> getRpcCheckerPoolList(fields, pageable);
            case DashboardConstants.TILE_CLEARED_BY_USER -> getRpcClearedByUserList(fields, pageable);
            default -> Page.empty();
        };
    }

    /**
     * Stage/wfStage filter shared by the Maker's Pool and Checker's Pool lists and their
     * case-ageing counts, so the sidebar always totals the same population the list pages through.
     * pendingWfStage distinguishes the two pools ("RPCQUEUE" for Maker, "RPCCHECKERREVIEW" for Checker).
     */
    private Specification<TbObApplicationMaster> resolveRpcPoolStageSpecification(String subCategory,
                                                                                   String pendingWfStage) {
        String normalised = subCategory != null ? subCategory.toUpperCase() : "ALL";
        return switch (normalised) {
            case "NEW", "REWORK" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals(pendingWfStage));
            case "ON_HOLD" -> OnboardingSpecification.stageEquals("ONHOLD").and(OnboardingSpecification.wfStageEquals("RPCONHOLD"));
            default -> OnboardingSpecification.rpcPoolAllSpec("RPCQUEUE", pendingWfStage);
        };
    }

    /**
     * Base population (stage/wfStage + branch scope, no ageing/text-search) for whichever RPC tile
     * the request targets. Shared by the list and the case-ageing counts.
     */
    private Specification<TbObApplicationMaster> resolveRpcBaseSpecification(DashboardListRequestFields fields) {
        String tileType = fields.getTileType();
        Specification<TbObApplicationMaster> stageSpec = switch (tileType != null ? tileType : "") {
            case DashboardConstants.TILE_CHECKERS_POOL -> resolveRpcPoolStageSpecification(fields.getSubCategory(), "RPCCHECKERREVIEW");
            case DashboardConstants.TILE_CLEARED_BY_USER -> OnboardingSpecification.updatedByEquals(fields.getUserId())
                    .and(OnboardingSpecification.updatedTsAfter(LocalDate.now().atStartOfDay()));
            default -> resolveRpcPoolStageSpecification(fields.getSubCategory(), "RPCQUEUE");
        };
        return stageSpec.and(OnboardingSpecification.branchIdIn(fields.getBranchIds()));
    }

    /**
     * Day-wise case ageing for the RPC (Maker) sidebar, scoped to the requested tileType/subCategory.
     */
    private List<CaseAgeingCount> getRpcCaseAgeingCounts(DashboardListRequestFields fields) {
        Specification<TbObApplicationMaster> baseSpec = resolveRpcBaseSpecification(fields);
        List<CaseAgeingCount> ageingCounts = new ArrayList<>();
        for (int bucket = 0; bucket <= DashboardConstants.CASE_AGEING_ABOVE_BUCKET; bucket++) {
            long count = onboardingRepository.count(baseSpec.and(resolveAgeingSpecification(bucket)));
            ageingCounts.add(new CaseAgeingCount(bucket, count));
        }
        return ageingCounts;
    }

    private Page<?> getRpcTlInHoDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        if(DashboardConstants.TILE_MAKERS_POOL.equals(fields.getTileType())) {
            return getRpcMakerPoolList(fields, pageable);
        }
        if (DashboardConstants.TILE_CHECKERS_POOL.equals(fields.getTileType())) {
            return getRpcCheckerPoolList(fields, pageable);
        }

        if (DashboardConstants.TILE_GREEN_CHANNEL.equals(fields.getTileType())) {
            return getRpcGreenChannelList(fields, pageable);
        }
        if (DashboardConstants.TILE_CLEARED_CASES.equals(fields.getTileType())) {
            return getRpcClearedCasesList(fields, pageable);
        }
        return getRpcDashboardList(fields, pageable);
    }


    private Page<ApplicationSummary> getRpcClearedCasesList(DashboardListRequestFields fields, Pageable pageable) {
        return onboardingRepository.findAll(OnboardingSpecification.stageIn(DashboardConstants.RPC_CLEARED_STAGES)
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

      private Page<?> getRpcAmlHoDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Integer caseAgingDays = fields.getCaseAgingDays();
        LocalDateTime beforeDate = (caseAgingDays != null) ? LocalDateTime.now().minusDays(caseAgingDays) : null;
        List<String> branchIds = fields.getBranchIds();
        Specification<TbObApplicationMaster> stageSpec = switch (subCategory) {
            case "PENDING" -> OnboardingSpecification.stageEquals("AMLQUEUE");
            case "APPROVED" -> OnboardingSpecification.stageEquals("BREQUEUE");
            case "REJECTED" -> OnboardingSpecification.stageEquals("AMLREJECTED");
            default -> OnboardingSpecification.stageIn(List.of("AMLQUEUE", "BREQUEUE", "AMLREJECTED"));
        };
        Specification<TbObApplicationMaster> spec = stageSpec
                .and(OnboardingSpecification.branchIdIn(branchIds))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        if (beforeDate != null) {
            spec = spec.and(OnboardingSpecification.createdTsBefore(beforeDate));
        }
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<?> getCrtDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        if (DashboardConstants.FLOW_DEFAULT.equalsIgnoreCase(fields.getFlow())) {
            log.info("CRT default flow for userId: {} - returning counts only, no list", fields.getUserId());
            return Page.empty(pageable);
        }
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.stageIn(resolveCrtStages(fields.getSubCategory()))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        if (fields.getAgeingAsInt() != null) {
            spec = spec.and(resolveAgeingSpecification(fields.getAgeingAsInt()));
        }
        return onboardingRepository.findAll(spec, pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    /**
     * Stages backing each CRT sub-category tab. Shared by the list and the case-ageing counts so
     * the sidebar always totals the same population the list pages through.
     */
    private List<String> resolveCrtStages(String subCategory) {
        String normalised = subCategory != null ? subCategory.toUpperCase() : "ALL";
        return switch (normalised) {
            case "PENDING" -> DashboardConstants.CRT_PENDING_STAGES;
            case "APPROVED" -> DashboardConstants.CRT_APPROVED_STAGES;
            case "REJECTED" -> DashboardConstants.CRT_REJECTED_STAGES;
            default -> DashboardConstants.CRT_ALL_STAGES;
        };
    }

    /**
     * Restricts the list to a single case-ageing bucket: 0-5 selects applications whose last
     * activity falls on that exact calendar day, 6 selects everything older than 5 days.
     */
    private Specification<TbObApplicationMaster> resolveAgeingSpecification(int ageing) {
        LocalDate today = LocalDate.now();
        if (ageing >= DashboardConstants.CASE_AGEING_ABOVE_BUCKET) {
            return OnboardingSpecification.lastActivityBefore(
                    today.minusDays(DashboardConstants.CASE_AGEING_ABOVE_BUCKET - 1L).atStartOfDay());
        }
        LocalDateTime dayStart = today.minusDays(ageing).atStartOfDay();
        return OnboardingSpecification.lastActivityBetween(dayStart, dayStart.plusDays(1));
    }

    /**
     * Day-wise case ageing for the CRT sidebar, scoped to the requested sub-category.
     */
    private List<CaseAgeingCount> getCrtCaseAgeingCounts(DashboardListRequestFields fields) {
        Map<Integer, Long> countsByBucket = onboardingRepository
                .countCaseAgeingBucketsByStageIn(resolveCrtStages(fields.getSubCategory()),
                        DashboardConstants.CASE_AGEING_ABOVE_BUCKET)
                .stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));

        List<CaseAgeingCount> ageingCounts = new ArrayList<>();
        for (int bucket = 0; bucket <= DashboardConstants.CASE_AGEING_ABOVE_BUCKET; bucket++) {
            ageingCounts.add(new CaseAgeingCount(bucket, countsByBucket.getOrDefault(bucket, 0L)));
        }
        return ageingCounts;
    }

   private Page<ApplicationSummary> getRpcCheckerPoolList(DashboardListRequestFields fields, Pageable pageable) {
        Integer caseAgingDays = fields.getCaseAgingDays();
        LocalDateTime beforeDate = (caseAgingDays != null) ? LocalDateTime.now().minusDays(caseAgingDays) : null;
        Specification<TbObApplicationMaster> spec = resolveRpcPoolStageSpecification(fields.getSubCategory(), "RPCCHECKERREVIEW")
                .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        if (beforeDate != null) {
            spec = spec.and(OnboardingSpecification.createdTsBefore(beforeDate));
        }
        if (fields.getAgeingAsInt() != null) {
            spec = spec.and(resolveAgeingSpecification(fields.getAgeingAsInt()));
        }
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcClearedByUserList(DashboardListRequestFields fields, Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        Integer caseAgingDays = fields.getCaseAgingDays();
        LocalDateTime beforeDate = (caseAgingDays != null) ? LocalDateTime.now().minusDays(caseAgingDays) : null;
        Specification<TbObApplicationMaster> spec = OnboardingSpecification.updatedByEquals(fields.getUserId())
                .and(OnboardingSpecification.updatedTsAfter(startOfDay))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        if (beforeDate != null) {
            spec = spec.and(OnboardingSpecification.createdTsBefore(beforeDate));
        }
        if (fields.getAgeingAsInt() != null) {
            spec = spec.and(resolveAgeingSpecification(fields.getAgeingAsInt()));
        }
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getRpcGreenChannelList(DashboardListRequestFields fields, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(greenChannelAgeDays);
        return onboardingRepository.findAll(OnboardingSpecification.channelTypeEquals(DashboardConstants.CHANNEL_TYPE_GREEN)
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.createdTsAfter(since))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getRpcMakerPoolList(DashboardListRequestFields fields, Pageable pageable) {
        Integer caseAgingDays = fields.getCaseAgingDays();
        LocalDateTime beforeDate = (caseAgingDays != null) ? LocalDateTime.now().minusDays(caseAgingDays) : null;
        Specification<TbObApplicationMaster> spec = resolveRpcPoolStageSpecification(fields.getSubCategory(), "RPCQUEUE")
                .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                .and(OnboardingSpecification.buildSearchSpecification(fields));
        if (beforeDate != null) {
            spec = spec.and(OnboardingSpecification.createdTsBefore(beforeDate));
        }
        if (fields.getAgeingAsInt() != null) {
            spec = spec.and(resolveAgeingSpecification(fields.getAgeingAsInt()));
        }
        return onboardingRepository.findAll(spec, pageable).map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getDraftTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec;
        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION" -> stageSpec = OnboardingSpecification.stageEquals("DRAFT");
            case "CB_QUEUE" -> stageSpec = OnboardingSpecification.stageEquals("BREQUEUE");
            case "UNNATI_PENDING" -> stageSpec = OnboardingSpecification.stageEquals("OPENMARKET");
            case "PHOTO_DEDUPE_FAIL" -> {
                return Page.empty();
            }
            default -> stageSpec = OnboardingSpecification.stageIn(List.of("DRAFT", "BREQUEUE", "OPENMARKET"));
        }
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCbFailTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec = switch (subCategory) {
            case "PENDING_WITH_KM" -> OnboardingSpecification.stageEquals("BREQUEUE");
            case "PENDING_WITH_CRT" -> OnboardingSpecification.stageEquals("CRTQUEUE");
            default -> OnboardingSpecification.stageIn(List.of("BREQUEUE", "CRTQUEUE"));
        };
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getRpcQueueTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> spec = switch (subCategory) {
            case "PENDING_WITH_MAKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPC_QUEUE"));
            case "PENDING_WITH_CHECKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPCCHECKERREVIEW"));
            default -> OnboardingSpecification.stageEquals("RPCQUEUE");
        };
        return onboardingRepository.findAll(spec
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getInputLoanDetailsTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMER" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("LOAN")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }


    private Page<ApplicationSummary> getOnholdTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            case "PUSH_BACK_FROM_BM" -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("CGT")
                            .and(OnboardingSpecification.wfStageEquals("BMPUSHBACK"))
                            .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case "PUSH_BACK_FROM_AM" -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("CGT")
                            .and(OnboardingSpecification.wfStageEquals("AMPUSHBACK"))
                            .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            default -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("ONHOLD")
                            .and(OnboardingSpecification.wfStageEquals("RPCONHOLD"))
                            .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
        };
    }

    private Page<ApplicationSummary> getReinterviewTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("BMQUEUE")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.branchIdIn(fields.getBranchIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getActivatedRejectedTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        List<String> branchIds = fields.getBranchIds();
        Specification<TbObApplicationMaster> statusSpec = switch (subCategory) {
            case "ACTIVATED" -> OnboardingSpecification.activatedSpec();
            case "REJECTED" -> OnboardingSpecification.rejectedSpec(DashboardConstants.REJECT_STAGES);
            default -> OnboardingSpecification.activatedOrRejectedSpec(DashboardConstants.REJECT_STAGES);
        };
        return onboardingRepository.findAll(OnboardingSpecification.branchIdIn(branchIds)
                        .and(statusSpec)
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<KendraSummary> getNewKendraAndGroupTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObKendra> statusSpec = switch (subCategory) {
            case "PENDING_FOR_ACTIVATION" -> KendraSpecification.statusEquals("PENDING");
            case "ACTIVE" -> KendraSpecification.statusEquals("ACTIVE");
            default -> KendraSpecification.statusIn(List.of("PENDING", "ACTIVE"));
        };
        Page<TbObKendra> kendras = kendraRepository.findAll(statusSpec
                .and(KendraSpecification.branchIdIn(fields.getBranchIds()))
                .and(KendraSpecification.buildSearchSpecification(fields)), pageable);
        return kendras.map(this::convertToKendraSummary);
    }


    private Page<ApplicationSummary> getActivatedRejectedTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        List<String> kendraIds = fields.getKendraIds();
        Specification<TbObApplicationMaster> statusSpec = switch (subCategory) {
            case "ACTIVATED" -> OnboardingSpecification.activatedSpec();
            case "REJECTED" -> OnboardingSpecification.rejectedSpec(DashboardConstants.REJECT_STAGES);
            default -> OnboardingSpecification.activatedOrRejectedSpec(DashboardConstants.REJECT_STAGES);
        };
        return onboardingRepository.findAll(OnboardingSpecification.kendraIdIn(kendraIds)
                        .and(statusSpec)
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcQueueTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> spec = switch (subCategory) {
            case "PENDING_WITH_MAKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPCQUEUE"));
            case "PENDING_WITH_CHECKER" -> OnboardingSpecification.stageEquals("RPCQUEUE").and(OnboardingSpecification.wfStageEquals("RPCCHECKERREVIEW"));
            default -> OnboardingSpecification.stageEquals("RPCQUEUE");
        };
        return onboardingRepository.findAll(spec
                        .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }
    private Page<ApplicationSummary> getCbFailTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec = switch (subCategory) {
            case "PENDING_WITH_KM" -> OnboardingSpecification.stageEquals("BREQUEUE");
            case "PENDING_WITH_CRT" -> OnboardingSpecification.stageEquals("CRTQUEUE");
            default -> OnboardingSpecification.stageIn(List.of("BREQUEUE", "CRTQUEUE"));
        };
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getInputLoanDetailsTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageEquals("LOAN")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

     private Page<ApplicationSummary> getOnholdTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        return switch (subCategory) {
            case "PUSH_BACK_FROM_BM" -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("CGT")
                            .and(OnboardingSpecification.wfStageEquals("BMPUSHBACK"))
                            .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            case "PUSH_BACK_FROM_AM" -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("CGT")
                            .and(OnboardingSpecification.wfStageEquals("AMPUSHBACK"))
                            .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
            default -> onboardingRepository.findAll(OnboardingSpecification.stageEquals("ONHOLD")
                            .and(OnboardingSpecification.wfStageEquals("RPCONHOLD"))
                            .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                            .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
        };//need to correct/RPC_ONHOLD
    }

    private Page<ApplicationSummary> getReinterviewTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> recordTypeSpec = switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS" -> OnboardingSpecification.recordTypeEquals("REPLACEMENT");
            case "NEW" -> OnboardingSpecification.recordTypeEquals("NEW");
            default -> OnboardingSpecification.recordTypeIn(List.of("REPLACEMENT", "NEW"));
        };
        return onboardingRepository.findAll(OnboardingSpecification.stageOrStatusEquals("BMQUEUE")
                        .and(recordTypeSpec)
                        .and(OnboardingSpecification.kendraIdIn(fields.getKendraIds()))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getDraftTileList(DashboardListRequestFields fields, Pageable pageable) {
        List<String> kendraIds = fields.getKendraIds();
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Specification<TbObApplicationMaster> stageSpec;

        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION" -> stageSpec = OnboardingSpecification.stageEquals("DRAFT");
            case "CB_QUEUE" -> stageSpec = OnboardingSpecification.stageEquals("BREQUEUE");
            case "UNNATI_PENDING" -> stageSpec = OnboardingSpecification.stageEquals("OPENMARKET");
            case "PHOTO_DEDUPE_FAIL" -> {
                return Page.empty();
            }
            default -> stageSpec = OnboardingSpecification.stageIn(List.of("DRAFT", "BREQUEUE", "OPENMARKET"));
        }
        return onboardingRepository.findAll(stageSpec
                        .and(OnboardingSpecification.kendraIdIn(kendraIds))
                        .and(OnboardingSpecification.buildSearchSpecification(fields)), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }
    private Page<KendraSummary> getNewKendraAndGroupTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        List<String> kendraIds = fields.getKendraIds();
        Specification<TbObKendra> kendraStatusSpec;
        Specification<TbObGroup> groupStatusSpec;
        switch (subCategory) {
            case "PENDING_FOR_ACTIVATION" -> {
                kendraStatusSpec = KendraSpecification.statusEquals("PENDING");
                groupStatusSpec = GroupSpecification.statusEquals("PENDING");
            }
            case "ACTIVE" -> {
                kendraStatusSpec = KendraSpecification.statusEquals("ACTIVE");
                groupStatusSpec = GroupSpecification.statusEquals("ACTIVE");
            }
            default -> {
                List<String> allStatuses = List.of("PENDING", "ACTIVE");
                kendraStatusSpec = KendraSpecification.statusIn(allStatuses);
                groupStatusSpec = GroupSpecification.statusIn(allStatuses);
            }
        }

        Page<TbObKendra> kendras = kendraRepository.findAll(kendraStatusSpec
                .and(KendraSpecification.kendraIdIn(kendraIds))
                .and(KendraSpecification.buildSearchSpecification(fields)), pageable);
        Page<TbObGroup> groups = groupRepository.findAll(groupStatusSpec
                .and(GroupSpecification.kendraIdIn(kendraIds))
                .and(GroupSpecification.buildSearchSpecification(fields)), pageable);

        List<KendraSummary> combined = new ArrayList<>();
        kendras.forEach(kendra -> combined.add(convertToKendraSummary(kendra)));
        groups.forEach(group -> combined.add(convertGroupToKendraSummary(group)));

        long totalElements = kendras.getTotalElements() + groups.getTotalElements();
        return new PageImpl<>(combined, pageable, totalElements);
    }

    private KendraSummary convertToKendraSummary(TbObKendra kendra) {
        return new KendraSummary(
                kendra.getKendraId(),
                kendra.getKendraName(),
                kendra.getStatus(),
                kendra.getCreatedTs(),
                "KENDRA"
        );
    }

    private KendraSummary convertGroupToKendraSummary(TbObGroup group) {
        return new KendraSummary(
                group.getGroupId(),
                group.getGroupName(),
                group.getStatus(),
                group.getCreatedTs(),
                "GROUP"
        );
    }
}
