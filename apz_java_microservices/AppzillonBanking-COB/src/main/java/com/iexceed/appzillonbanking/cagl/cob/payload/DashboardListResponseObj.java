package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Ensure null fields are not serialized
public class DashboardListResponseObj {
    @JsonProperty("appList")
    private List<?> appList;
    @JsonProperty("pageNo")
    private int pageNo;
    @JsonProperty("totalPendingMbdfs")
    private long totalPendingMbdfs;
    @JsonProperty("pageSize")
    private int pageSize;
    @JsonProperty("totalElements")
    private long totalElements;
    @JsonProperty("totalPages")
    private int totalPages;
    @JsonProperty("appCountList")
    private List<TileCount> appCountList;
    @JsonProperty("caseAgeingList")
    private List<CaseAgeingCount> caseAgeingList;

    public DashboardListResponseObj(Page<?> page, List<TileCount> tiles) {
        this.appList = page.getContent();
        this.pageNo = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.totalPendingMbdfs = tiles == null ? 0L : tiles.stream()
                .filter(tile -> "Total Pending MBDFs".equals(tile.getTileName()))
                .findFirst()
                .map(TileCount::getCount)
                .orElse(0L);
        this.appCountList = tiles == null ? null : tiles.stream()
                .filter(tile -> !"Total Pending MBDFs".equals(tile.getTileName()))
                .collect(Collectors.toList());
    }
}
