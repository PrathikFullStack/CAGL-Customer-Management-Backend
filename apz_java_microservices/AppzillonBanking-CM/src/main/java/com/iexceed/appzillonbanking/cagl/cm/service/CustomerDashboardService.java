package com.iexceed.appzillonbanking.cagl.cm.service;

import com.iexceed.appzillonbanking.cagl.cm.payload.dashboard.CmDashboardSummaryDto;

public interface CustomerDashboardService {

    /**
     * Calculates tile counts for Action Required and Overview dashboard categories
     *
     * @param role     Logged-in user role (KM, BM, AM, RPC, etc.)
     * @param branchId Branch identifier or name
     * @return Aggregated metrics DTO for dashboard tiles
     */
    CmDashboardSummaryDto getDashboardSummary(String role, String branchId);
}
