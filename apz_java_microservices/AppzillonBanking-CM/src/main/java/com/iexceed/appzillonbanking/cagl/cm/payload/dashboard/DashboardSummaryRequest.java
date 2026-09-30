package com.iexceed.appzillonbanking.cagl.cm.payload.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryRequest {
    private String tab;
    private String subTab;
    private String search;
    private Integer pageNo;
    private Integer pageSize;
}
