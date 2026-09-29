package com.iexceed.appzillonbanking.cagl.cm.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.dashboard.CmDashboardSummaryDto;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cm/dashboard")
@Tag(name = "1. Customer Dashboard", description = "Endpoints for Customer Management Landing Page summary counts and metrics")
public class CustomerDashboardRestController {

    private static final Logger logger = LogManager.getLogger(CustomerDashboardRestController.class);

    private final CustomerDashboardService dashboardService;

    public CustomerDashboardRestController(CustomerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves summary tile counts for Existing Customer Management Dashboard
     * Covers Action Required (Drafts, Onhold, Campaign) and Overview (Pending BM/AM/RPC, Completed, Rejected, Photo Dedupe, T24)
     */
    @GetMapping("/summary")
    @Operation(summary = "Get Dashboard Tile Summary Counts", description = "Fetches counts for all Action Required and Overview tiles based on user role and branch")
    public ResponseEntity<ResponseWrapper<CmDashboardSummaryDto>> getDashboardSummary(
            @Parameter(description = "User Role filter (e.g. KM, BM, AM, RPCMAKER, CHT)")
            @RequestParam(required = false, defaultValue = "KM") String role,
            @Parameter(description = "Branch ID or Branch Name (leave empty to view all branches, or specify e.g. Ejipura)")
            @RequestParam(required = false) String branchId,
            @Parameter(hidden = true)
            @RequestHeader(value = "userRole", required = false) String userRoleHeader,
            @Parameter(hidden = true)
            @RequestHeader(value = "branchId", required = false) String branchIdHeader) {

        String effectiveRole = (role != null && !role.isBlank() && !role.equalsIgnoreCase("role")) ? role : (userRoleHeader != null ? userRoleHeader : "KM");
        String effectiveBranch = (branchId != null && !branchId.isBlank() && !branchId.equalsIgnoreCase("branchId")) ? branchId : branchIdHeader;

        logger.info("Fetching Dashboard Summary for Role: {}, Branch: {}", effectiveRole, effectiveBranch);

        CmDashboardSummaryDto summary = dashboardService.getDashboardSummary(effectiveRole, effectiveBranch);
        return ResponseEntity.ok(ResponseWrapper.success(summary, "Dashboard summary counts fetched successfully"));
    }
}
