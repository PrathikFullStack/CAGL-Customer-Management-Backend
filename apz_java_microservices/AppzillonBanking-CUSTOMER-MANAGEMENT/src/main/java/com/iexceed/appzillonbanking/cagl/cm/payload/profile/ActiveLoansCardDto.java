package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveLoansCardDto {
    private List<LoanItemDto> activeLoans;
    private List<EligibleProductDto> eligibleProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanItemDto {
        private String loanId;
        private String product;
        private String amount;
        private String outstandingPrincipal;
        private String interestRate;
        private String overdueStatus;
        private String maturityDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibleProductDto {
        private String productName;
        private Double eligibleAmount;
        private Double cbEligibleAmount;
    }
}
