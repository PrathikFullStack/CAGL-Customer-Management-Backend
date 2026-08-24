package com.iexceed.appzillonbanking.cagl.cob.service;
import com.iexceed.appzillonbanking.cagl.cob.constants.DashboardConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.mapper.ApplicationSummaryMapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.OnboardingRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLeadRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCGTDetailsRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGRTRepository;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final TbObCGTDetailsRepository cgtDetailsRepository;
    private final TbObGRTRepository grtRepository;



    @Value("${cob.dashboard.tablet.page-size:10}")
    private int defaultPageSize;

    @Value("${dashboard.list.default-sort-by:createdTs}")
    private String defaultSortBy;

    @Value("${dashboard.list.default-sort-order:ASC}")
    private String defaultSortOrder;

    public DashboardResponse getDashboardCounts(DashboardRequest request, Header header) {
        DashboardRequestFields fields = request.getReqObj();
        if (fields != null && "KM".equalsIgnoreCase(fields.getUserRole())) {
            return getKmDashboardCounts(fields.getKendraIds(), fields.getUserId());
        }
        if (fields != null && "BM".equalsIgnoreCase(fields.getUserRole())) {
            return getBmDashboardCounts(fields.getBranchId());
        }
        if (fields != null && isRpcRole(fields.getUserRole())) {
            return getRpcDashboardCounts(fields.getBranchIds(), fields.getUserId());
        }
        if (fields != null && "CRT".equalsIgnoreCase(fields.getUserRole())) {
            return getCrtDashboardCounts();
        }
        return new DashboardResponse(new ArrayList<>());
    }

    private DashboardResponse getKmDashboardCounts(List<String> kendraIds, String userId) {
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount("Leads", leadRepository.countByCreatedBy(userId)));
        long drafts = onboardingRepository.countByWfStageAndKendraIdIn("DRAFT", kendraIds);
        long cbFail = onboardingRepository.countByWfStageInAndKendraIdIn(List.of("BREINPROGRESS", "CRTQUEUE"), kendraIds);
        long pendingWithRpc = onboardingRepository.countByStatusAndKendraIdIn("RPCQUEUE", kendraIds);

        long inputLoanDetails = onboardingRepository.countByRecordTypeInAndKendraIdIn(List.of("REPLACEMENT", "NEW"), kendraIds);
        long rpcOnHold = onboardingRepository.countByWfStageAndStatusInAndKendraIdIn("RPCONHOLD", List.of("RPCQUEUE", "BMQUEUE", "AMQUEUE"), kendraIds);

        List<String> groupIds = cgtDetailsRepository.findGroupIdsByStatusAndKendraIdIn("PENDING", kendraIds);
        long cgt = onboardingRepository.countByGroupIdIn(groupIds);

        long reinterview = onboardingRepository.countByStatusAndRecordTypeInAndKendraIdIn("BMQUEUE", List.of("REPLACEMENT", "NEW"), kendraIds);

        List<String> grtGroupIds = grtRepository.findGroupIdsByStatus("PENDING");
        long pendingForGrt = onboardingRepository.countByGroupIdIn(grtGroupIds);

        tiles.add(new TileCount(DashboardConstants.TILE_DRAFT, drafts));
        tiles.add(new TileCount(DashboardConstants.TILE_BRE_FAIL, cbFail));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_WITH_RPC, pendingWithRpc));
        tiles.add(new TileCount(DashboardConstants.TILE_INPUT_LOAN_DETAILS, inputLoanDetails));
        tiles.add(new TileCount(DashboardConstants.TILE_ONHOLD, rpcOnHold));
        tiles.add(new TileCount(DashboardConstants.TILE_CGT_IN_PROGRESS, cgt));
        tiles.add(new TileCount(DashboardConstants.TILE_REINTERVIEW, reinterview));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_GRT, pendingForGrt));
        long totalPendingMbdfs = drafts + cbFail + pendingWithRpc + inputLoanDetails + rpcOnHold + cgt + reinterview + pendingForGrt;
        tiles.add(new TileCount("Total Pending MBDFs", totalPendingMbdfs));
        tiles.add(new TileCount(DashboardConstants.TILE_REJECTED, onboardingRepository.countByStatusAndKendraIdIn("REJECTED", kendraIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_ACTIVATED, onboardingRepository.countByStatusAndKendraIdIn("ACTIVATED", kendraIds)));
        tiles.add(new TileCount(DashboardConstants.TILE_REACTIVATION, onboardingRepository.countByRecordTypeAndKendraIdIn(DashboardConstants.RECORD_TYPE_REACTIVATION, kendraIds)));
        tiles.add(new TileCount("New Kendras", kendraRepository.countByStatusAndCreatedByAndKendraIdIn("ACTIVE", userId, kendraIds)));
        tiles.add(new TileCount("New Groups", groupRepository.countByStatusAndCreatedByAndKendraIdIn("ACTIVE", userId, kendraIds)));
        return new DashboardResponse(tiles);
    }

    private DashboardResponse getBmDashboardCounts(String branchId) {
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount("Leads", leadRepository.countByBranchId(branchId)));
        long drafts = onboardingRepository.countByWfStageAndBranchId("DRAFT", branchId);
        long cbFail = onboardingRepository.countByWfStageInAndBranchId(List.of("BREINPROGRESS", "CRTQUEUE"), branchId);
        long pendingWithRpc = onboardingRepository.countByStatusAndBranchId("RPCQUEUE", branchId);

        long inputLoanDetails = onboardingRepository.countByRecordTypeInAndBranchId(List.of("REPLACEMENT", "NEW"), branchId);
        long rpcOnHold = onboardingRepository.countByWfStageAndStatusInAndBranchId("RPCONHOLD", List.of("RPCQUEUE", "BMQUEUE", "AMQUEUE"), branchId);

        List<String> groupIds = cgtDetailsRepository.findGroupIdsByStatusAndBranchId("PENDING", branchId);
        long cgt = onboardingRepository.countByGroupIdIn(groupIds);

        long reinterview = onboardingRepository.countByStatusAndRecordTypeInAndBranchId("BMQUEUE", List.of("REPLACEMENT", "NEW"), branchId);

        List<String> grtGroupIds = grtRepository.findGroupIdsByStatus("PENDING");
        long pendingForGrt = onboardingRepository.countByGroupIdIn(grtGroupIds);

        tiles.add(new TileCount(DashboardConstants.TILE_DRAFT, drafts));
        tiles.add(new TileCount(DashboardConstants.TILE_BRE_FAIL, cbFail));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_WITH_RPC, pendingWithRpc));
        tiles.add(new TileCount(DashboardConstants.TILE_INPUT_LOAN_DETAILS, inputLoanDetails));
        tiles.add(new TileCount(DashboardConstants.TILE_ONHOLD, rpcOnHold));
        tiles.add(new TileCount(DashboardConstants.TILE_CGT_IN_PROGRESS, cgt));
        tiles.add(new TileCount(DashboardConstants.TILE_REINTERVIEW, reinterview));
        tiles.add(new TileCount(DashboardConstants.TILE_PENDING_FOR_GRT, pendingForGrt));
        long totalPendingMbdfs = drafts + cbFail + pendingWithRpc + inputLoanDetails + rpcOnHold + cgt + reinterview + pendingForGrt;
        tiles.add(new TileCount("Total Pending MBDFs", totalPendingMbdfs));
        tiles.add(new TileCount(DashboardConstants.TILE_REJECTED, onboardingRepository.countByStatusAndBranchId("REJECTED", branchId)));
        tiles.add(new TileCount(DashboardConstants.TILE_ACTIVATED, onboardingRepository.countByStatusAndBranchId("ACTIVATED", branchId)));
        tiles.add(new TileCount(DashboardConstants.TILE_REACTIVATION, onboardingRepository.countByRecordTypeAndBranchId(DashboardConstants.RECORD_TYPE_REACTIVATION, branchId)));
        tiles.add(new TileCount("New Kendras", kendraRepository.countByStatusAndBranchId("ACTIVE", branchId)));
        tiles.add(new TileCount("New Groups", groupRepository.countByStatusAndBranchId("ACTIVE", branchId)));
        return new DashboardResponse(tiles);
    }

    private boolean isRpcRole(String userRole) {
        return userRole != null && DashboardConstants.RPC_ROLES.contains(userRole.trim().toUpperCase());
    }

    private DashboardResponse getRpcDashboardCounts(List<String> branchIds, String userId) {
        List<TileCount> tiles = new ArrayList<>();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        tiles.add(new TileCount("Maker’s Pool - Pending", onboardingRepository.countByStatusAndBranchIdIn("RPC_MAKER_PENDING", branchIds)));
        tiles.add(new TileCount("Maker’s Pool - Onhold", onboardingRepository.countByStatusAndBranchIdIn("RPCONHOLD", branchIds)));
        tiles.add(new TileCount("Checker’s Pool - Pending", onboardingRepository.countByStatusAndBranchIdIn("RPC_CHECKER_REVIEW", branchIds)));
        tiles.add(new TileCount("Checker’s Pool - Onhold", onboardingRepository.countByStatusAndBranchIdIn("RPC_CHECKER_ONHOLD", branchIds)));
        tiles.add(new TileCount("Cleared by user - Maker", onboardingRepository.countByStatusAndUpdatedByAndUpdatedTsAfter("RPC_INREVIEW", userId, startOfDay)));
        tiles.add(new TileCount("Cleared by user - Checker", onboardingRepository.countByStatusAndUpdatedByAndUpdatedTsAfter("RPC_CHECKER_CLEARED", userId, startOfDay)));
        return new DashboardResponse(tiles);
    }

    private DashboardResponse getCrtDashboardCounts() {
        long crtUserPoolCount = onboardingRepository.countByWfStage("CRTQUEUE");
        List<TileCount> tiles = new ArrayList<>();
        tiles.add(new TileCount("CRT User Pool", crtUserPoolCount));
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
        auditService.logDashboardViewEvent(fields);

        DashboardRequest countRequest = new DashboardRequest();
        DashboardRequestFields countRequestFields = new DashboardRequestFields();
        countRequestFields.setUserId(fields.getUserId());
        countRequestFields.setUserRole(fields.getUserRole());
        countRequestFields.setBranchId(fields.getBranchId());
        countRequestFields.setKendraIds(fields.getKendraIds());
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
        if ("BM".equalsIgnoreCase(userRole)) {
            log.info("Routing to getBmDashboardList for branchId: {}", fields.getBranchId());
            resultPage = getBmDashboardList(fields, pageable);
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

    private Page<?> getKmDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        Page<?> resultPage;
        List<String> kendraIds = fields.getKendraIds(); // Directly use kendraIds from fields

        switch (fields.getTileType()) {
            case DashboardConstants.TILE_DRAFT:
                resultPage = getDraftTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_BRE_FAIL:
                resultPage = getCbFailTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_RPC_QUEUE:
                resultPage = getRpcQueueTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_CGT_IN_PROGRESS:
                List<String> groupIds = cgtDetailsRepository.findGroupIdsByStatusAndKendraIdIn("PENDING", fields.getKendraIds());
                resultPage = onboardingRepository.findByGroupIdIn(groupIds, pageable).map(applicationSummaryMapper::toApplicationSummary);
                break;
            case DashboardConstants.TILE_ACTIVATED_REJECTED_TODAY:
                resultPage = getActivatedRejectedTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_CRT_PENDING:
                if ("CRT".equalsIgnoreCase(fields.getUserRole())) {
                    resultPage = getCrtPendingApplications(fields.getUserId(), pageable);
                } else {
                    resultPage = Page.empty();
                }
                break;
            case DashboardConstants.TILE_INPUT_LOAN_DETAILS:
                resultPage = getInputLoanDetailsTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_ONHOLD:
                resultPage = getOnholdTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_REINTERVIEW:
                resultPage = getReinterviewTileList(fields, pageable);
                break;
            case DashboardConstants.TILE_PENDING_FOR_ACTIVATION:
                resultPage = onboardingRepository.findByStatusAndKendraIdInAndLoanIdIsNull("ACTIVATED", kendraIds, pageable)
                        .map(applicationSummaryMapper::toApplicationSummary);
                break;
            case DashboardConstants.TILE_PENDING_FOR_GRT:
                List<String> grtGroupIds = grtRepository.findGroupIdsByStatus("PENDING");
                resultPage = onboardingRepository.findByGroupIdIn(grtGroupIds, pageable).map(applicationSummaryMapper::toApplicationSummary);
                break;
            case "New Kendras":
            case DashboardConstants.TILE_NEW_KENDRA_AND_GROUP:
                resultPage = getNewKendraAndGroupTileList(fields, pageable);
                break;
            case "New Groups":
                resultPage = Page.empty();
                break;
            default:
                resultPage = Page.empty();
        }
        return resultPage;
    }

    private Page<?> getBmDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        Page<?> resultPage;
        switch (fields.getTileType()) {
            case DashboardConstants.TILE_DRAFT:
                resultPage = getDraftTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_BRE_FAIL:
                resultPage = getCbFailTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_BM_QUEUE:
                resultPage = onboardingRepository.findByStageAndBranchId(DashboardConstants.STAGE_BM, fields.getBranchId(), pageable)
                        .map(applicationSummaryMapper::toApplicationSummary);
                break;
            case DashboardConstants.TILE_RPC_QUEUE:
                resultPage = getRpcQueueTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_CGT_IN_PROGRESS:
                List<String> groupIds = cgtDetailsRepository.findGroupIdsByStatusAndBranchId("PENDING", fields.getBranchId());
                resultPage = onboardingRepository.findByGroupIdIn(groupIds, pageable).map(applicationSummaryMapper::toApplicationSummary);
                break;
            case DashboardConstants.TILE_ACTIVATED_REJECTED_TODAY:
                resultPage = getActivatedRejectedTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_INPUT_LOAN_DETAILS:
                resultPage = getInputLoanDetailsTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_ONHOLD:
                resultPage = getOnholdTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_REINTERVIEW:
                resultPage = getReinterviewTileListForBm(fields, pageable);
                break;
            case DashboardConstants.TILE_PENDING_FOR_ACTIVATION:
                resultPage = onboardingRepository.findByStatusAndBranchIdAndLoanIdIsNull("ACTIVATED", fields.getBranchId(), pageable)
                        .map(applicationSummaryMapper::toApplicationSummary);
                break;
            case DashboardConstants.TILE_PENDING_FOR_GRT:
                List<String> grtGroupIds = grtRepository.findGroupIdsByStatus("PENDING");
                resultPage = onboardingRepository.findByGroupIdIn(grtGroupIds, pageable).map(applicationSummaryMapper::toApplicationSummary);
                break;
            case "New Kendras":
            case DashboardConstants.TILE_NEW_KENDRA_AND_GROUP:
                resultPage = getNewKendraAndGroupTileListForBm(fields, pageable);
                break;
            case "New Groups":
                resultPage = Page.empty();
                break;
            default:
                resultPage = Page.empty();
        }
        return resultPage;
    }

    private Page<?> getRpcDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        Page<?> resultPage;
        switch (fields.getTileType()) {
            case "Maker’s Pool":
                resultPage = getRpcMakerPoolList(fields, pageable);
                break;
            case "Checker’s Pool":
                resultPage = getRpcCheckerPoolList(fields, pageable);
                break;
            case "Cleared by user":
                resultPage = getRpcClearedByUserList(fields, pageable);
                break;
            case DashboardConstants.TILE_GREEN_SUBMITTED:
                resultPage = getRpcGreenChannelList(fields, pageable);
                break;

            default:
                resultPage = Page.empty();
        }
        return resultPage;
    }

    private Page<?> getCrtDashboardList(DashboardListRequestFields fields, Pageable pageable) {
        Page<TbObApplicationMaster> resultPage = onboardingRepository.findByStatus("CB_QUEUE", pageable);
        return resultPage.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcCheckerPoolList(DashboardListRequestFields fields, Pageable pageable) {
        return onboardingRepository.findByStatusAndBranchIdIn("RPC_CHECKER_REVIEW", fields.getBranchIds(), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcClearedByUserList(DashboardListRequestFields fields, Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return onboardingRepository.findByStatusAndUpdatedByAndUpdatedTsAfter("RPC_INREVIEW", fields.getUserId(), startOfDay, pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcGreenChannelList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        if ("ON_HOLD".equals(subCategory)) {
            return onboardingRepository.findByStatusAndChannelTypeAndBranchIdIn("RPCONHOLD", DashboardConstants.CHANNEL_TYPE_GREEN, fields.getBranchIds(), pageable)
                    .map(applicationSummaryMapper::toApplicationSummary);
        }
        return onboardingRepository.findByStatusAndChannelTypeAndBranchIdIn("RPC_PENDING", DashboardConstants.CHANNEL_TYPE_GREEN, fields.getBranchIds(), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcMakerPoolList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        Integer caseAgingDays = fields.getCaseAgingDays();
        LocalDateTime beforeDate = (caseAgingDays != null) ? LocalDateTime.now().minusDays(caseAgingDays) : null;
        List<String> branchIds = fields.getBranchIds();

        switch (subCategory) {
            case "NEW":
                applications = (beforeDate != null)
                        ? onboardingRepository.findByStatusAndBranchIdInAndCreatedTsBefore("RPC_MAKER_PENDING", branchIds, beforeDate, pageable)
                        : onboardingRepository.findByStatusAndBranchIdIn("RPC_MAKER_PENDING", branchIds, pageable);
                break;
            case "REWORK":
                applications = (beforeDate != null)
                        ? onboardingRepository.findByStatusAndBranchIdInAndCreatedTsBefore("RPCPUSHBACK", branchIds, beforeDate, pageable)
                        : onboardingRepository.findByStatusAndBranchIdIn("RPCPUSHBACK", branchIds, pageable);
                break;
            case "ON_HOLD":
                applications = (beforeDate != null)
                        ? onboardingRepository.findByStatusAndBranchIdInAndCreatedTsBefore("RPCONHOLD", branchIds, beforeDate, pageable)
                        : onboardingRepository.findByStatusAndBranchIdIn("RPCONHOLD", branchIds, pageable);
                break;
            default: // "ALL"
                List<String> allMakerStatuses = List.of("RPC_MAKER_PENDING", "RPCPUSHBACK", "RPCONHOLD");
                applications = (beforeDate != null)
                        ? onboardingRepository.findByStatusInAndBranchIdInAndCreatedTsBefore(allMakerStatuses, branchIds, beforeDate, pageable)
                        : onboardingRepository.findByStatusInAndBranchIdIn(allMakerStatuses, branchIds, pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getDraftTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION":
                applications = onboardingRepository.findByWfStageAndBranchId("DRAFT", fields.getBranchId(), pageable);
                break;
            case "CB_QUEUE":
                applications = onboardingRepository.findByStatusAndBranchId("BREINPROGRESS", fields.getBranchId(), pageable);
                break;
            case "REDIRECTED_FROM_UNNATI":
                applications = onboardingRepository.findByStatusAndBranchId("UNNATI_REDIRECT", fields.getBranchId(), pageable);
                break;
            case "PHOTO_DEDUPE_FAIL":
                applications = onboardingRepository.findByStatusAndBranchId("PHOTO_DEDUPE_FAIL", fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allDraftWfStages = List.of("DRAFT", "BREINPROGRESS", "UNNATI_REDIRECT", "PHOTO_DEDUPE_FAIL");
                applications = onboardingRepository.findByWfStageInAndBranchId(allDraftWfStages, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCbFailTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "PENDING_WITH_KM":
                applications = onboardingRepository.findByWfStageAndBranchId("BREINPROGRESS", fields.getBranchId(), pageable);
                break;
            case "PENDING_WITH_CRT":
                applications = onboardingRepository.findByWfStageAndBranchId("CRTQUEUE", fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allCbFailStatuses = List.of("BREINPROGRESS", "CRTQUEUE");
                applications = onboardingRepository.findByWfStageInAndBranchId(allCbFailStatuses, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcQueueTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "PENDING_WITH_MAKER":
                applications = onboardingRepository.findByStatusAndBranchId("RPCQUEUE", fields.getBranchId(), pageable);
                break;
            case "PENDING_WITH_CHECKER":
                applications = onboardingRepository.findByStatusAndBranchId("RPCQUEUE", fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allRpcStatuses = List.of("RPCQUEUE", "RPCQUEUE");
                applications = onboardingRepository.findByStatusInAndBranchId(allRpcStatuses, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getInputLoanDetailsTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS":
                applications = onboardingRepository.findByRecordTypeAndBranchId("REPLACEMENT", fields.getBranchId(), pageable);
                break;
            case "NEW":
                applications = onboardingRepository.findByRecordTypeAndBranchId("NEW", fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allLoanInputTypes = List.of("REPLACEMENT", "NEW");
                applications = onboardingRepository.findByRecordTypeInAndBranchId(allLoanInputTypes, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getOnholdTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "RPC_ONHOLD":
                applications = onboardingRepository.findByWfStageAndStatusAndBranchId("ONHOLD", "RPCQUEUE", fields.getBranchId(), pageable);
                break;
            case "PUSH_BACK_FROM_BM":
                applications = onboardingRepository.findByWfStageAndStatusAndBranchId("ONHOLD", "BMQUEUE", fields.getBranchId(), pageable);
                break;
            case "PUSH_BACK_FROM_AM":
                applications = onboardingRepository.findByWfStageAndStatusAndBranchId("ONHOLD", "AMQUEUE", fields.getBranchId(), pageable);
                break;
            default: // "ALL"
                List<String> allOnholdStatuses = List.of("RPCQUEUE", "BMQUEUE", "AMQUEUE");
                applications = onboardingRepository.findByWfStageAndStatusInAndBranchId("ONHOLD", allOnholdStatuses, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getReinterviewTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "REPLACEMENT":
                applications = onboardingRepository.findByStageAndRecordTypeAndBranchId(DashboardConstants.STAGE_BM, "REPLACEMENT", fields.getBranchId(), pageable);
                break;
            case "NEW":
                applications = onboardingRepository.findByStageAndRecordTypeAndBranchId(DashboardConstants.STAGE_BM, "NEW", fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allReinterviewTypes = List.of("REPLACEMENT", "NEW");
                applications = onboardingRepository.findByStageAndRecordTypeInAndBranchId(DashboardConstants.STAGE_BM, allReinterviewTypes, fields.getBranchId(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getActivatedRejectedTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        List<String> statuses;

        switch (subCategory) {
            case "ACTIVATED":
                statuses = List.of("ACTIVATED");
                break;
            case "REJECTED":
                statuses = List.of("REJECTED");
                break;
            case "ALL":
            default:
                statuses = List.of("ACTIVATED", "REJECTED");
                break;
        }
        return onboardingRepository.findByStatusInAndBranchId(statuses, fields.getBranchId(), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<KendraSummary> getNewKendraAndGroupTileListForBm(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObKendra> kendras;
        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION":
                kendras = kendraRepository.findByStatusAndBranchId("PENDING", fields.getBranchId(), pageable);
                break;
            case "PENDING_FOR_ACTIVATION":
                kendras = kendraRepository.findByStatusAndBranchId("ACTIVE", fields.getBranchId(), pageable);
                break;
            case "NEW_KENDRAS":
                kendras = kendraRepository.findActiveKendrasWithCapacityForBranch(fields.getBranchId(), pageable);
                break;
            case "ALL":
            default:
                List<String> allKendraStatuses = List.of("PENDING", "ACTIVE");
                kendras = kendraRepository.findByStatusInAndBranchId(allKendraStatuses, fields.getBranchId(), pageable);
                break;
        }
        return kendras.map(this::convertToKendraSummary);
    }

    private Page<ApplicationSummary> getActivatedRejectedTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        List<String> statuses;

        switch (subCategory) {
            case "ACTIVATED":
                statuses = List.of("ACTIVATED");
                break;
            case "REJECTED":
                statuses = List.of("REJECTED");
                break;
            case "ALL":
            default:
                statuses = List.of("ACTIVATED", "REJECTED");
                break;
        }
        return onboardingRepository.findByStatusInAndKendraIdIn(statuses, fields.getKendraIds(), pageable)
                .map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getRpcQueueTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;

        switch (subCategory) {
            case "PENDING_WITH_MAKER":
                applications = onboardingRepository.findByStatusAndKendraIdIn("RPCQUEUE", fields.getKendraIds(), pageable);
                break;
            case "PENDING_WITH_CHECKER":
                applications = onboardingRepository.findByStatusAndKendraIdIn("RPCQUEUE", fields.getKendraIds(), pageable);
                break;
            case "ALL":
            default:
                List<String> allRpcStatuses = List.of("RPCQUEUE", "RPCQUEUE");
                applications = onboardingRepository.findByStatusInAndKendraIdIn(allRpcStatuses, fields.getKendraIds(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getCbFailTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;

        switch (subCategory) {
            case "PENDING_WITH_KM":
                applications = onboardingRepository.findByWfStageAndKendraIdIn("BREINPROGRESS", fields.getKendraIds(), pageable);
                break;
            case "PENDING_WITH_CRT":
                applications = onboardingRepository.findByWfStageAndKendraIdIn("CRTQUEUE", fields.getKendraIds(), pageable);
                break;
            case "ALL":
            default:
                List<String> allCbFailStatuses = List.of("BREINPROGRESS", "CRTQUEUE");
                applications = onboardingRepository.findByWfStageInAndKendraIdIn(allCbFailStatuses, fields.getKendraIds(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getInputLoanDetailsTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;

        switch (subCategory) {
            case "REPLACEMENT_CUSTOMERS":
                // Fetches only replacement members needing loan details.
                applications = onboardingRepository.findByRecordTypeAndKendraIdIn("REPLACEMENT", fields.getKendraIds(), pageable);
                break;
            case "NEW":
                applications = onboardingRepository.findByRecordTypeAndKendraIdIn("NEW", fields.getKendraIds(), pageable);
                break;
            case "ALL":
            default:
                List<String> allLoanInputTypes = List.of("REPLACEMENT", "NEW");
                applications = onboardingRepository.findByRecordTypeInAndKendraIdIn(allLoanInputTypes, fields.getKendraIds(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getOnholdTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;

        switch (subCategory) {
            case "RPC_ONHOLD":
                // Applications put on hold by RPC users.
                applications = onboardingRepository.findByWfStageAndStatusAndKendraIdIn("ONHOLD", "RPCQUEUE", fields.getKendraIds(), pageable);
                break;
            case "PUSH_BACK_FROM_BM":
                // Applications pushed back from BM to CGT stage.
                applications = onboardingRepository.findByWfStageAndStatusAndKendraIdIn("ONHOLD", "BMQUEUE", fields.getKendraIds(), pageable);
                break;
            case "PUSH_BACK_FROM_AM":
                // Applications pushed back from AM to CGT stage.
                applications = onboardingRepository.findByWfStageAndStatusAndKendraIdIn("ONHOLD", "AMQUEUE", fields.getKendraIds(), pageable);
                break;
            default: // "ALL"
                List<String> allOnholdStatuses = List.of("RPCQUEUE", "BMQUEUE", "AMQUEUE");
                applications = onboardingRepository.findByWfStageAndStatusInAndKendraIdIn("ONHOLD", allOnholdStatuses, fields.getKendraIds(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getReinterviewTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;
        switch (subCategory) {
            case "REPLACEMENT":
                applications = onboardingRepository.findByStageAndRecordTypeAndKendraIdIn(DashboardConstants.STAGE_BM, "REPLACEMENT", fields.getKendraIds(), pageable);
                break;
            case "NEW":
                applications = onboardingRepository.findByStageAndRecordTypeAndKendraIdIn(DashboardConstants.STAGE_BM, "NEW", fields.getKendraIds(), pageable);
                break;
            case "ALL":
            default:
                List<String> allReinterviewTypes = List.of("REPLACEMENT", "NEW");
                applications = onboardingRepository.findByStageAndRecordTypeInAndKendraIdIn(DashboardConstants.STAGE_BM, allReinterviewTypes, fields.getKendraIds(), pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<ApplicationSummary> getDraftTileList(DashboardListRequestFields fields, Pageable pageable) {
        List<String> kendraIds = fields.getKendraIds();
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObApplicationMaster> applications;

        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION":
                applications = onboardingRepository.findByWfStageAndKendraIdIn("DRAFT", kendraIds, pageable);
                break;
            case "CB_QUEUE":
                applications = onboardingRepository.findByStatusAndKendraIdIn("BREINPROGRESS", kendraIds, pageable);
                break;
            case "REDIRECTED_FROM_UNNATI":
                applications = onboardingRepository.findByStatusAndKendraIdIn("UNNATI_REDIRECT", kendraIds, pageable);
                break;
            case "PHOTO_DEDUPE_FAIL":
                applications = onboardingRepository.findByStatusAndKendraIdIn("PHOTO_DEDUPE_FAIL", kendraIds, pageable);
                break;
            case "ALL":
            default:
                List<String> allDraftWfStages = List.of("DRAFT", "BREINPROGRESS", "UNNATI_REDIRECT", "PHOTO_DEDUPE_FAIL");
                applications = onboardingRepository.findByWfStageInAndKendraIdIn(allDraftWfStages, kendraIds, pageable);
                break;
        }
        return applications.map(applicationSummaryMapper::toApplicationSummary);
    }

    private Page<KendraSummary> getNewKendraAndGroupTileList(DashboardListRequestFields fields, Pageable pageable) {
        String subCategory = fields.getSubCategory() != null ? fields.getSubCategory().toUpperCase() : "ALL";
        Page<TbObKendra> kendras;
        switch (subCategory) {
            case "PENDING_FOR_SUBMISSION":
                kendras = kendraRepository.findByStatusAndKendraIdIn("PENDING", fields.getKendraIds(), pageable);
                break;
            case "PENDING_FOR_ACTIVATION":
                kendras = kendraRepository.findByStatusAndKendraIdIn("ACTIVE", fields.getKendraIds(), pageable);
                break;
            case "NEW_KENDRAS":
                kendras = kendraRepository.findActiveKendrasWithCapacity(fields.getKendraIds(), pageable);
                break;
            case "ALL":
            default:
                List<String> allKendraStatuses = List.of("PENDING", "ACTIVE");
                kendras = kendraRepository.findByStatusInAndKendraIdIn(allKendraStatuses, fields.getKendraIds(), pageable);
                break;
        }
        return kendras.map(this::convertToKendraSummary);
    }

    private KendraSummary convertToKendraSummary(TbObKendra kendra) {
        return new KendraSummary(
                kendra.getKendraId(),
                kendra.getKendraName(),
                kendra.getStatus(),
                kendra.getCreatedTs()
        );
    }

    private Page<CrtApplicationSummary> getCrtPendingApplications(String userId, Pageable pageable) {
        List<String> assignedKendraIds = getAssignedKendraIdsForCrtUser(userId);
        return onboardingRepository.findByStatusInAndKendraIdIn(List.of("CRTQUEUE"), assignedKendraIds, pageable)
                .map(this::convertToCrtApplicationSummary);
    }

    private CrtApplicationSummary convertToCrtApplicationSummary(TbObApplicationMaster app) {
        ApplicationSummary summary = applicationSummaryMapper.toApplicationSummary(app);
        long agingDays = 0;
        if (summary.getCreatedTs() != null) {
            agingDays = ChronoUnit.DAYS.between(summary.getCreatedTs(), LocalDateTime.now());
        }
        return new CrtApplicationSummary(summary, agingDays);
    }

    private List<String> getAssignedKendraIdsForCrtUser(String userId) {
        return List.of(""); // Example Kendra IDs
    }
}