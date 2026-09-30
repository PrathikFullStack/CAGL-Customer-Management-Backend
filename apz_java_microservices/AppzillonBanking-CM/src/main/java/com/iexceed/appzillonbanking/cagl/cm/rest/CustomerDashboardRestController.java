package com.iexceed.appzillonbanking.cagl.cm.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Customer Management Dashboard", description = "Endpoints for fetching complete dashboard summary counts and customer data in one shot")
public class CustomerDashboardRestController {

    private static final Logger logger = LogManager.getLogger(CustomerDashboardRestController.class);

    private final CustomerDashboardService dashboardService;

    public CustomerDashboardRestController(CustomerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get All Dashboard Summary & Member Data (All-In-One Shot)",
        description = "Returns complete summary tile counts (Action Required & Overview) AND all table lists for Drafts (all/online/offline), Onhold (all/fromBm/fromAm/fromRpc), Campaign drive, and Overview categories in a single call."
    )
    public ResponseEntity<ResponseWrapper<CmDashboardSummaryDto>> getDashboardSummary(
            @Parameter(description = "User Role (e.g. KM, BM, AM, RPCMAKER)") @RequestParam(required = false, defaultValue = "KM") String role,
            @Parameter(description = "Branch ID or Branch Name filter") @RequestParam(required = false) String branchId,
            @Parameter(description = "Optional Search query by member name, member ID, kendra, or KM") @RequestParam(required = false) String search) {

        logger.info("Fetching All-In-One Dashboard Summary and Data for Role: {}, Branch: {}, Search: {}",
                role, branchId, search);

        CmDashboardSummaryDto response = dashboardService.getDashboardSummaryAllInOne(role, branchId, search);

        return ResponseEntity.ok(ResponseWrapper.success(response, "Dashboard summary and all records fetched successfully"));
    }
}
