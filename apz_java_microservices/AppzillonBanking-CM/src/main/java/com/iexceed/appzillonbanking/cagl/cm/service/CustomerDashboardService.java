package com.iexceed.appzillonbanking.cagl.cm.service;

import com.iexceed.appzillonbanking.cagl.cm.payload.dashboard.CmDashboardSummaryDto;

public interface CustomerDashboardService {


    CmDashboardSummaryDto getDashboardSummary(String role, String branchId);
}
